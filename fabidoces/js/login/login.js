document.getElementById('login-button').addEventListener('click', async (event) => {
    event.preventDefault();
    const client = {
        email: document.getElementById('email').value,
        senha: document.getElementById('senha').value
    };
    

    const response = await fetch(`http://localhost:8080/api/client/login?email=${client.email}&senha=${client.senha}`, {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json'
        },
    });

    if (response.ok) {
        window.location.href = 'http://127.0.0.1:5500/fabidoces/index.html';
    } else {
        const error = await response.text();
        alert(error);
}
});


document.getElementById('github-login-button').addEventListener('click', () => {
    event.preventDefault();
    const width = 600;
    const height = 700;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;
    
    const authWindow = window.open(
        'https://github.com/login/oauth/authorize?client_id=Ov23lioj0u58ESOeyZUs&scope=user:email',
        'GitHubLogin',
        `width=${width},height=${height},top=${top},left=${left},resizable=yes,scrollbars=yes,toolbar=no,menubar=no,location=no`
    );
    
    // Opcional: Verifica se a janela foi bloqueada
    if (!authWindow || authWindow.closed || typeof authWindow.closed === 'undefined') {
        alert('Por favor, permita popups para este site');
    }
});