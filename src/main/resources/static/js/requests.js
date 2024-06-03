function logout() {
    fetch('/auth/logout')
        .then(response => {
            if (response.ok) {
                window.location.href = '/auth/login';
            } else {
                console.error('Logout failed with status:', response.status);
            }
        })
        .catch(error => {
            console.error('Logout failed with error:', error);
        });
}

function grantAdmin() {
    const username = document.getElementById('username').value;
    if (!username) {
        alert('Please enter a username.');
        return;
    }

    fetch('/api/grantAdmin?username=' + encodeURIComponent(username), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
        .then(response => response.text())
        .then(text => {
            alert(text);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error processing request');
        });
}

function applyCustomPath() {
    const path = document.getElementById('searcherPath').value;
    if (!path) {
        alert('Please enter a path.');
        return;
    }

    fetch('/api/submitCustomPath?searcherPath=' + encodeURIComponent(path), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    }).catch(error => {
        console.error('Error:', error);
    });

    setTimeout(() => {
        window.location.href = '/';
    }, 2000);
}

function dropCustomPath() {
    fetch('/api/dropCustomPath', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    }).catch(error => {
        console.error('Error:', error);
    });

    setTimeout(() => {
        window.location.href = '/';
    }, 2000);
}

function resetFileDatabase() {
    fetch('/api/resetFileDatabase', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to reset database with status:', response.status);
        }
    }).catch(error => {
        console.error('Reset database failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function resetDatabase() {
    fetch('/api/resetDatabase', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to reset database with status:', response.status);
        }
    }).catch(error => {
        console.error('Reset database failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function restartServer() {
    fetch('/api/restartServer', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to restart server with status:', response.status);
        }
    }).catch(error => {
        console.error('Restart server failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function setDummyFiles() {
    fetch('/api/setDummyFiles', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to add dummy files with status:', response.status);
        }
    }).catch(error => {
        console.error('Add dummy files failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function changePassword() {
    const nickname = document.getElementById('nickname').value;
    const oldPassword = document.getElementById('old-password').value;
    const newPassword = document.getElementById('new-password').value;

    const formData = new URLSearchParams();
    if (nickname) {
        formData.append('nickname', encodeURIComponent(nickname));
    }

    formData.append('oldPassword', encodeURIComponent(oldPassword));
    formData.append('newPassword', encodeURIComponent(newPassword));

    fetch('/api/changeCredentials?' + formData, {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log('Password changed successfully:', text));
            alert('Credentials updated successfully!');
        } else {
            console.error('Failed to change password with status:', response.status);
            response.text().then(text => alert(text));
        }
    }).catch(error => {
        console.error('Change password failed with error:', error);
        alert('Change password failed with error: ' + error);
    });
}

function downloadFile(filePath) {
    window.location.href = `/api/download?filePath=${encodeURIComponent(filePath)}`;
}

function removeFile(id, isReviewPage) {
    const link = isReviewPage ? '/api/removeTempFile?id=' : '/api/removeFile?id='
    fetch(link + id, {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            console.error('Failed to remove file with status:', response.status);
        }
    }).catch(error => {
        console.error('Failed to remove file:', error);
    });
}

function approveFile(button) {
    const reviewDialog = document.getElementById('reviewDialog');
    reviewDialog.style.display = 'block';

    const id = button.firstElementChild.getAttribute('data-id')

    fetch('/api/approve?id=' + id, {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            console.error('Failed to approve file with status:', response.status);
        }
    }).catch(error => {
        console.error('Failed to approve file:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 1500);
}

function refuseFile(button) {
    const reviewDialog = document.getElementById('reviewDialog');
    reviewDialog.style.display = 'block';

    const id = button.firstElementChild.getAttribute('data-id')

    fetch('/api/refuse?id=' + id, {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            console.error('Failed to refuse file with status:', response.status);
        }
    }).catch(error => {
        console.error('Failed to refuse file:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 1500);
}

function submitForm() {
    const formData = new FormData(document.getElementById('uploadForm'));

    fetch('/api/upload', {
        method: 'POST',
        body: formData,
    })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                document.getElementById('error-message').innerText = data.error;
                document.getElementById('error-message').style.display = 'block';
            } else {
                document.getElementById('error-message').style.display = 'none';
                window.location.href = '/search';
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function deleteFileFromHistory(filePath) {
    // Send a GET request to the server to delete the file from history
    const encodedFilePath = encodeURIComponent(filePath);
    fetch('/api/remove?filePath=' + encodedFilePath)
        .then(response => {
            if (!response.ok) {
                console.error('Failed to delete file from history: ' + encodedFilePath);
            }

            // Refresh the file history dialog
            const currentFilePath = document.querySelector('#historyButton').getAttribute('onclick').match(/'([^']+)'/)[1];
            showFileHistory(currentFilePath);
        });
}

function updateFile(id, isReviewPage) {
    const updateLink = isReviewPage ? '/api/searchTempFile?id=' : '/api/searchFile?id='
    fetch(updateLink + id)
        .then(response => response.json())
        .then(data => {
            if (data.user != null) {
                openUpdateDialog(data.id, data.decNumber, data.deviceName, data.documentType, data.usedDevices, data.project, data.inventoryNumber, data.location, data.lastModified, data.creationTime, data.user.username, isReviewPage)
            } else {
                console.error('Error:', data);
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}