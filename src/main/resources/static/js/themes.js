let systemThemeListener;

function initTheme() {
    const selectedTheme = localStorage.getItem("selected-theme");
    const themeDropdown = document.getElementById("theme-dropdown");
    const accentColor = localStorage.getItem('accent-color');

    if (selectedTheme && themeDropdown) {
        themeDropdown.value = selectedTheme;
    } else {
        systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
        systemThemeListener.addEventListener("change", systemThemeChange);
    }

    const colorPicker = document.getElementById("accentColor");
    if (colorPicker) {
        if (accentColor) {
            colorPicker.value = accentColor;
        } else {
            colorPicker.value = getDefaultAccentColor();
        }
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
        systemThemeListener.addEventListener("change", systemThemeChange);
    }
}

function changeTheme() {
    const themeDropdown = document.getElementById("theme-dropdown");
    const selectedTheme = themeDropdown ? themeDropdown.value : localStorage.getItem("selected-theme") || "system";
    const accentColor = localStorage.getItem('accent-color');

    if (selectedTheme !== "system") {
        if (systemThemeListener) {
            systemThemeListener.removeEventListener("change", systemThemeChange);
            systemThemeListener = null;
        }
        document.documentElement.setAttribute("data-theme", selectedTheme);
    } else {
        if (!systemThemeListener) {
            systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
            systemThemeListener.addEventListener("change", systemThemeChange);
        }
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        document.documentElement.setAttribute("data-theme", prefersDark ? "dark" : "light");
    }

    localStorage.setItem("selected-theme", selectedTheme);

    if (accentColor) {
        changePrimaryColor(accentColor);
    }
}

function changePrimaryColor(baseColor) {
    const hoverColor = shadeColor(baseColor, -20);

    document.documentElement.style.setProperty('--primary-color', baseColor);
    document.documentElement.style.setProperty('--primary-color-hover', hoverColor);
    document.documentElement.style.setProperty('--link-color', baseColor);
    document.documentElement.style.setProperty('--settings-button-background-color', baseColor);
    document.documentElement.style.setProperty('--settings-button-hover-background-color', hoverColor);

    localStorage.setItem("accent-color", baseColor);
}

function resetAccentColor() {
    document.getElementById('accentColor').value = getDefaultAccentColor();
    localStorage.removeItem('accent-color');
    document.getElementById("colorPicker").value = '';
    document.getElementById("accentColor").disabled = false;
    document.documentElement.style.removeProperty('--primary-color');
    document.documentElement.style.removeProperty('--primary-color-hover');
    document.documentElement.style.removeProperty('--link-color');
    document.documentElement.style.removeProperty('--settings-button-background-color');
    document.documentElement.style.removeProperty('--settings-button-hover-background-color');
}

function shadeColor(color, percent) {
    let R = parseInt(color.substring(1, 3), 16);
    let G = parseInt(color.substring(3, 5), 16);
    let B = parseInt(color.substring(5, 7), 16);

    R = parseInt(R * (100 + percent) / 100);
    G = parseInt(G * (100 + percent) / 100);
    B = parseInt(B * (100 + percent) / 100);

    R = (R < 255) ? R : 255;
    G = (G < 255) ? G : 255;
    B = (B < 255) ? B : 255;

    const RR = ((R.toString(16).length === 1) ? "0" + R.toString(16) : R.toString(16));
    const GG = ((G.toString(16).length === 1) ? "0" + G.toString(16) : G.toString(16));
    const BB = ((B.toString(16).length === 1) ? "0" + B.toString(16) : B.toString(16));

    return "#" + RR + GG + BB;
}


function initThemeButtons() {
    const savedColor = localStorage.getItem("accent-color") || '#007bff';

    const colorPicker = document.getElementById("accentColor");
    const colorSelect = document.getElementById("colorPicker");

    colorPicker.value = savedColor;
    colorSelect.value = colorSelect.querySelector(`option[value="${savedColor}"]`) ? savedColor : '';

    colorPicker.disabled = !!colorSelect.value;
}

function handleAccentColorPicker() {
    const colorPicker = document.getElementById("accentColor");
    const colorSelect = document.getElementById("colorPicker");
    let newColor;

    if (colorSelect.value !== "") {
        newColor = colorSelect.value;
        colorPicker.disabled = true;
    } else {
        newColor = colorPicker.value;
        colorPicker.disabled = false;
    }

    if (colorSelect.value === "") {
        resetAccentColor();
        return;
    }

    changePrimaryColor(newColor);
    colorPicker.value = newColor;
    localStorage.setItem("accent-color", newColor);
}

function handleAccentColorHex() {
    const colorPicker = document.getElementById("accentColor");
    let newColor;

    if (colorPicker.value) {
        newColor = colorPicker.value;
    } else {
        newColor = getDefaultAccentColor();
    }

    if (isValidColor(newColor)) {
        changePrimaryColor(newColor);
        colorPicker.value = newColor;
        localStorage.setItem("accent-color", newColor);
    } else {
        alert("Please enter a valid HEX color code.");
        colorPicker.value = localStorage.getItem("accent-color") || '#007bff';
    }
}

function isValidColor(color) {
    return /^#[0-9A-F]{6}$/i.test(color);
}

function getDefaultAccentColor() {
    const theme = document.documentElement.getAttribute('data-theme');
    switch (theme) {
        case 'dark':
            return '#555';
        case 'light':
            return '#007bff';
        default:
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            return prefersDark ? '#555' : '#007bff';
    }
}