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

function removeFile(filePath) {
    window.location.href = `/api/remove?filePath=${encodeURIComponent(filePath)}`;
    window.location.reload();
}