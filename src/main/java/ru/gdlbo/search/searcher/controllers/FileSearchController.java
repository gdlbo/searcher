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
import ru.gdlbo.search.searcher.repository.PaginatedResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Controller
public class FileSearchController {
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

        List<FileInfo> fileInfos = performFileSearch(query, showHidden);

        sortFileInfos(fileInfos, sortBy, sortOrder, sortByLastModified);

        if (number != null && !number.isEmpty()) {
            fileInfos = filterFileInfosByNumber(fileInfos, query, number);
        }

        PaginatedResult paginatedResult = paginateFileInfos(fileInfos, page, 50);

        addAttributesToModel(model, paginatedResult, query, authentication, sortByLastModified, showHidden);

        return "search";
    }

    private List<FileInfo> performFileSearch(String query, Boolean showHidden) {
        List<FileInfo> fileInfos = Collections.synchronizedList(new ArrayList<>());
        String searchPath = config.getPath();

        if (searchPath == null || searchPath.isEmpty()) {
            return fileInfos;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            File defaultLocation = new File(searchPath);
            searchDirectory(defaultLocation, "", fileInfos, executor, futures, showHidden);
        } else {
            File searchLocation = new File(searchPath);
            searchDirectory(searchLocation, query, fileInfos, executor, futures, showHidden);
        }

        executor.shutdown();
        return fileInfos;
    }

    private void sortFileInfos(List<FileInfo> fileInfos, String sortBy, String sortOrder, Boolean sortByLastModified) {
        if (sortBy != null && !sortBy.isEmpty()) {
            fileInfos.sort(getFileInfoComparator(sortBy, sortOrder, sortByLastModified));
        } else {
            fileInfos.sort(getFileInfoComparator("name", "asc", sortByLastModified));
        }
    }

    private List<FileInfo> filterFileInfosByNumber(List<FileInfo> fileInfos, String query, String number) {
        Pattern numberPattern = Pattern.compile("ВГМТ\\." + number + "\\.\\d{3}(-\\d{2})?.*");
        return fileInfos.stream()
                .filter(fileInfo -> {
                    boolean matchesQuery = query == null || query.isEmpty() || fileInfo.getFilePath().contains(query);
                    boolean matchesNumber = numberPattern.matcher(fileInfo.getFilePath()).find();
                    return matchesQuery && matchesNumber;
                })
                .toList();
    }

    private PaginatedResult paginateFileInfos(List<FileInfo> fileInfos, int page, int resultsPerPage) {
        int startIndex = page * resultsPerPage;
        int endIndex;
        int totalPages;

        if (fileInfos.isEmpty()) {
            totalPages = 0;
            endIndex = 0;
        } else {
            totalPages = (int) Math.ceil((double) fileInfos.size() / resultsPerPage);
            startIndex = Math.min(startIndex, fileInfos.size());
            endIndex = Math.min(startIndex + resultsPerPage, fileInfos.size());
        }

        if (startIndex > endIndex) {
            startIndex = endIndex;
        }

        List<FileInfo> paginatedFileInfos = fileInfos.subList(startIndex, endIndex);

        return new PaginatedResult(paginatedFileInfos, fileInfos.size() > endIndex, page, totalPages);
    }

    private void addAttributesToModel(Model model, PaginatedResult paginatedResult, String query, Authentication authentication, Boolean sortByLastModified, Boolean showHidden) {
        model.addAttribute("fileInfos", paginatedResult.getPaginatedFileInfos());
        model.addAttribute("hasMoreResults", paginatedResult.isHasMoreResults());
        model.addAttribute("page", paginatedResult.getPage());
        model.addAttribute("pageNumbers", getPageNumbers(paginatedResult.getPage(), paginatedResult.getTotalPages(), 10));
        model.addAttribute("totalPages", paginatedResult.getTotalPages());
        model.addAttribute("query", query);
        model.addAttribute("isAdmin", authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        model.addAttribute("nickname", authentication.getName());
        model.addAttribute("sortByLastModified", sortByLastModified);
        model.addAttribute("showHidden", showHidden);
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
        if (directory != null && directory.isDirectory()) {
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
                .filter(file -> file.isFile() && (query == null || query.isEmpty() || file.getName().contains(query)) && (showHidden || !file.isHidden()))
                .toList();
    }

    private void submitTasksForDirectories(List<File> directories, String query, List<FileInfo> fileInfos, ExecutorService executor, List<Future<?>> futures, Boolean showHidden) {
        if (directories != null && !directories.isEmpty()) {
            directories.parallelStream().forEach(dir -> {
                Runnable task = createTask(dir, query, fileInfos, showHidden);
                futures.add(executor.submit(task));
            });
        }
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

    private Runnable createTask(File file, String query, List<FileInfo> fileInfos, Boolean showHidden) {
        return () -> {
            if (file.isDirectory()) {
                searchDirectory(file, query, fileInfos, Executors.newSingleThreadExecutor(), new ArrayList<>(), showHidden);
            }
        };
    }

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