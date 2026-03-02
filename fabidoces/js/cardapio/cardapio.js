import { adicionarAoCarrinho } from "../carrinho/carrinho.js";

export let catalogo = [];

export async function loadProducts() {
    try {
        const response = await fetch('http://localhost:8080/api/product/products', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        catalogo = data;
        return catalogo;
    } catch (error) {
        console.error('Error loading products:', error);
        alert(error.message);
        return [];
    }
}

const container = document.getElementById('container-composer');


function createProductElement(product) {

    if (!container) {
        console.error("Elemento #container-composer não encontrado!");
        return;
    }

    const productContainer = document.createElement('div');
    productContainer.classList.add('produto-container');

    const produtoDiv = document.createElement('div');
    produtoDiv.classList.add('produto');

    const imageContainer = document.createElement('div');
    imageContainer.classList.add('imagem-container-menu');

    const img = document.createElement('img');
    img.src = product.imagem;
    img.alt = product.nome;
    img.classList.add('img-produto-menu');

    const descricaoDiv = document.createElement('div');
    descricaoDiv.classList.add('descricao');

    const h3 = document.createElement('h3');
    h3.textContent = product.nome;

    const p = document.createElement('p');
    p.textContent = product.descricao;
    const p2 = document.createElement('span');
    p2.textContent = `Por apenas R$ ${product.preco.toFixed(2).replace('.', ',')} a unidade.`

    const button = document.createElement('button');
    button.classList.add('add-Carrinho');
    button.textContent = 'Adicionar ao Carrinho';

    button.onclick = function (event) {
        adicionarAoCarrinho(product.id, event);

    };
    descricaoDiv.appendChild(h3);
    descricaoDiv.appendChild(p);
    descricaoDiv.appendChild(p2)
    descricaoDiv.appendChild(button);

    imageContainer.appendChild(img);
    imageContainer.appendChild(descricaoDiv);

    produtoDiv.appendChild(imageContainer);
    productContainer.appendChild(produtoDiv);

    return productContainer;
}


document.addEventListener('DOMContentLoaded', () => {
    loadProducts().then(() => {
        catalogo.forEach(product => {
            const productElement = createProductElement(product);
            if (container) container.appendChild(productElement);
        });
    });
});

const faqBtn = document.getElementById("faq-btn");
const faqPopup = document.getElementById("faq-popup");
const closeFaq = document.getElementById("close-faq");

faqBtn.addEventListener("click", () => {
    faqPopup.style.display = "flex";
});

closeFaq.addEventListener("click", () => {
    faqPopup.style.display = "none";
});

window.addEventListener("click", (e) => {
    if (e.target === faqPopup) {
        faqPopup.style.display = "none";
    }
});