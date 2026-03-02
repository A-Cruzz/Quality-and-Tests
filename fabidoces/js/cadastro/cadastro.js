import { validaCPF } from "../validacoes/cpf.js";
import { validaSenha } from "../validacoes/senha.js"

async function criarUsuario(body) {
    try {
        const response = await fetch('http://localhost:8080/api/client/clients', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Trigger-Email': 'true'
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao cadastrar usuário');
        }

        return await response.json();

    } catch (error) {
        console.error('Erro na requisição:', error);
        throw error;
    }
}

const cpfInput = document.getElementById('cpf');
cpfInput.addEventListener('input', function () {
    let value = this.value.replace(/\D/g, '');
    if (value.length > 11) value = value.substring(0, 11);
    this.value = value.replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
});

const telefoneInput = document.getElementById('telefone');
telefoneInput.addEventListener('input', function () {
    let value = this.value.replace(/\D/g, '');
    if (value.length > 11) value = value.substring(0, 11);
    this.value = value.replace(/^(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{5})(\d{1,4})$/, '$1-$2');
});

document.getElementById('cadastro').addEventListener('submit', async function (event) {
    event.preventDefault();

    const body = {
        nome: document.getElementById('nome').value,
        email: document.getElementById('email').value,
        cpf: document.getElementById('cpf').value,
        telefone: document.getElementById('telefone').value,
        senha: document.getElementById('senha').value
    };

    const senhas = {
        senha: document.getElementById('senha').value,
        confirmaSenha: document.getElementById('confirma-senha').value
    };

    if (!validaCPF(body)) {
        return;
    }
    if (!validaSenha(senhas)) {
        return;
    }

    const btnSubmit = event.target.querySelector('button');
    const btnOriginalText = btnSubmit.value;

    btnSubmit.disabled = true;
    if (btnSubmit.tagName === 'INPUT') {
        btnSubmit.value = 'Cadastrando...';
    } else {
        btnSubmit.textContent = 'Cadastrando...';
    }

    try {
        const resultado = await criarUsuario(body);
        const primeiroNome = resultado.nome.split(' ')[0];
        alert(`Cadastro realizado! ${primeiroNome}, por gentileza verifique seu e-mail!`);
        event.target.reset();
        event.textContent = 'Cadastre-se'

    } catch (error) {
        alert(error.message || 'Erro no cadastro');
    } finally {
        btnSubmit.disabled = false;
        if (btnSubmit.tagName === 'INPUT') {
            btnSubmit.value = btnOriginalText;
        } else {
            btnSubmit.textContent = btnOriginalText;
        }
    }
});










