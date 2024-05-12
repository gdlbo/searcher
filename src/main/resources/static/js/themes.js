let systemThemeListener;

function initTheme() {
    const selectedTheme = localStorage.getItem("selected-theme");
    const themeDropdown = document.getElementById("theme-dropdown");

    if (selectedTheme && themeDropdown) {
        themeDropdown.value = selectedTheme;
    } else {
        systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
        systemThemeListener.addEventListener("change", systemThemeChange, {
            once: true
        });
    }

    changeTheme();
}

function systemThemeChange(event) {
    const newColorScheme = event.matches ? "dark" : "light";
    const themeDropdown = document.getElementById("theme-dropdown");
    document.documentElement.setAttribute("data-theme", newColorScheme);

    if (themeDropdown) {
        themeDropdown.value = "system";
    }

    localStorage.setItem("selected-theme", "system");

    if (!systemThemeListener) {
        systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
        systemThemeListener.addEventListener("change", systemThemeChange, {once: true});
    }
}

function changeTheme() {
    const themeDropdown = document.getElementById("theme-dropdown");
    const selectedTheme = themeDropdown ? themeDropdown.value : localStorage.getItem("selected-theme") || "system";

    if (selectedTheme !== "system") {
        if (systemThemeListener) {
            systemThemeListener.removeEventListener("change", systemThemeChange);
            systemThemeListener = null;
        }
        document.documentElement.setAttribute("data-theme", selectedTheme);
    } else {
        if (!systemThemeListener) {
            systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
            systemThemeListener.addEventListener("change", systemThemeChange, {
                once: true
            });
        }
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        document.documentElement.setAttribute("data-theme", prefersDark ? "dark" : "light");
    }

    localStorage.setItem("selected-theme", selectedTheme);
}