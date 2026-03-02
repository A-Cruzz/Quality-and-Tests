
export function mensagem(text, status = 'success') {
    Toastify({
        text: text,
        duration: 3000,
        style: {
            fontFamily: "'Roboto', 'Sans-Serif'",
            background: status === 'success' ? '#84cc16' : '#dc2626',
            boxShadow: 'none'
        }
    }).showToast();
}

