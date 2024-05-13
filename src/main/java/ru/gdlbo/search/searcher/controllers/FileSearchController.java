package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.config.Config;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

@Controller
public class FileSearchController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Config config;

    public static BasicFileAttributes getFileAttributes(File file) {
        Path path = file.toPath();
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return attrs;
    }

    // This method is responsible for searching files based on user input
    @GetMapping("/search")
    public String searchFiles(@RequestParam(required = false) String query,
                              @RequestParam(required = false) String number,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false, defaultValue = "asc") String sortOrder,
                              @RequestParam(required = false, defaultValue = "false") Boolean sortByLastModified,
                              @RequestParam(required = false, defaultValue = "false") Boolean showHidden,
                              Authentication authentication,
                              Model model) {

        List<FileInfo> fileInfos = Collections.synchronizedList(new ArrayList<>());
        int resultsPerPage = 50;
        String searchPath = config.getPath();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (searchPath == null || searchPath.isEmpty()) {
            return "start";
        }

        if (query == null || query.isEmpty()) {
            File defaultLocation = new File(searchPath);
            searchDirectory(defaultLocation, "", fileInfos, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), new ArrayList<>(), showHidden);
        } else {
            File searchLocation = new File(searchPath);
            searchDirectory(searchLocation, query, fileInfos, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), new ArrayList<>(), showHidden);
        }

        // Sort the fileInfos list based on user input
        if (sortBy != null && !sortBy.isEmpty()) {
            fileInfos.sort(getFileInfoComparator(sortBy, sortOrder, sortByLastModified));
        } else {
            fileInfos.sort(getFileInfoComparator("name", "asc", sortByLastModified));
        }

        // Filter the fileInfos list based on user input
        if (number != null && !number.isEmpty()) {
            Pattern numberPattern = Pattern.compile("ВГМТ\\." + number + "\\.\\d{3}(-\\d{2})?.*");

            fileInfos = fileInfos.stream()
                    .filter(fileInfo -> {
                        boolean matchesQuery = query == null || query.isEmpty() || fileInfo.getFilePath().contains(query);
                        boolean matchesNumber = numberPattern.matcher(fileInfo.getFilePath()).find();
                        return matchesQuery && matchesNumber;
                    })
                    .toList();
        }

        // Paginate the fileInfos list
        int startIndex = page * resultsPerPage;
        int endIndex = Math.min(startIndex + resultsPerPage, fileInfos.size());
        int totalPages = (int) Math.ceil((double) fileInfos.size() / resultsPerPage);
        List<FileInfo> paginatedFileInfos = fileInfos.subList(startIndex, endIndex);

        // Add the paginated fileInfos list and other data to the model
        model.addAttribute("fileInfos", paginatedFileInfos);
        model.addAttribute("hasMoreResults", fileInfos.size() > endIndex);
        model.addAttribute("page", page);
        model.addAttribute("pageNumbers", getPageNumbers(page, totalPages, 10));
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("query", query);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("nickname", authentication.getName());
        model.addAttribute("sortByLastModified", sortByLastModified);
        model.addAttribute("showHidden", showHidden);

        return "search";
    }

    private Comparator<FileInfo> getFileInfoComparator(String sortBy, String sortOrder, Boolean sortByLastModified) {
        Comparator<FileInfo> comparator = switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(FileInfo::getFilePath);
            case "date" -> sortByLastModified
                    ? Comparator.comparing(FileInfo::getLastModified)
                    : Comparator.comparing(FileInfo::getCreationTime);
            default -> throw new IllegalArgumentException("Invalid sortBy parameter: " + sortBy);
        };

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    // This method searches a directory for matching files and subdirectories
    private void searchDirectory(File directory, String query, List<FileInfo> fileInfos, ExecutorService executor, List<Future<?>> futures, Boolean showHidden) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                List<File> directories = getDirectories(files, showHidden);
                List<File> matchingFiles = getMatchingFiles(files, query, showHidden);

                submitTasksForDirectories(directories, query, fileInfos, executor, futures, showHidden);
                processMatchingFiles(matchingFiles, fileInfos);
            }
        }

        waitForTasksToComplete(futures);
    }

    private List<File> getDirectories(File[] files, Boolean showHidden) {
        return Arrays.stream(files)
                .filter(file -> file.isDirectory() && !file.getName().equals(".history") && (showHidden || !file.isHidden()))
                .toList();
    }

    private List<File> getMatchingFiles(File[] files, String query, Boolean showHidden) {
        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().contains(query) && (showHidden || !file.isHidden()))
                .toList();
    }

    private void submitTasksForDirectories(List<File> directories, String query, List<FileInfo> fileInfos, ExecutorService executor, List<Future<?>> futures, Boolean showHidden) {
        directories.parallelStream().forEach(dir -> {
            Runnable task = createTask(dir, query, fileInfos, showHidden);
            futures.add(executor.submit(task));
        });
    }

    private void processMatchingFiles(List<File> matchingFiles, List<FileInfo> fileInfos) {
        matchingFiles.parallelStream().forEach(file -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            BasicFileAttributes attrs = getFileAttributes(file);

            Date lastModifiedDate = attrs != null && attrs.lastModifiedTime() != null ? new Date(attrs.lastModifiedTime().toMillis()) : new Date(file.lastModified());
            String lastModified = dateFormat.format(lastModifiedDate);

            Date creationDate = attrs != null && attrs.creationTime() != null ? new Date(attrs.creationTime().toMillis()) : null;
            String creationDateStr = creationDate != null ? dateFormat.format(creationDate) : "N/A";

            synchronized (fileInfos) {
                fileInfos.add(new FileInfo(file.getAbsolutePath(), lastModified, creationDateStr));
            }
        });
    }

    // This method creates a Runnable task to search a directory
    private Runnable createTask(File file, String query, List<FileInfo> fileInfos, Boolean showHidden) {
        return () -> {
            if (file.isDirectory()) {
                searchDirectory(file, query, fileInfos, Executors.newSingleThreadExecutor(), new ArrayList<>(), showHidden);
            }
        };
    }

    // This method waits for all tasks to complete
    private void waitForTasksToComplete(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // This method returns a list of page numbers based on user input
    public List<Integer> getPageNumbers(int currentPage, int totalPages, int limit) {
        List<Integer> pageNumbers = new ArrayList<>();
        int startPage = Math.max(0, currentPage - limit / 2);
        int endPage = Math.min(startPage + limit, totalPages);

        if (currentPage > 0) {
            pageNumbers.add(-1);
        }

        for (int i = startPage; i < endPage; i++) {
            pageNumbers.add(i);
        }

        if (currentPage < totalPages - 1) {
            pageNumbers.add(totalPages);
        }

        return pageNumbers;
    }
}