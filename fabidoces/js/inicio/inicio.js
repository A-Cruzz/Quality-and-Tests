import { catalogo, loadProducts } from "../cardapio/cardapio.js";
import { adicionarAoCarrinho } from "../carrinho/carrinho.js";

if (catalogo.length === 0) {
    await loadProducts();
}

const catalogoProdutos = await loadProducts();


const carrosselWrapper = document.getElementById('carrossel');
const setaEsq = document.querySelector('.seta.esquerda');
const setaDir = document.querySelector('.seta.direita');

const ul = document.createElement('ul');
ul.classList.add('carrossel');
carrosselWrapper.appendChild(ul);

for (const [id, produto] of Object.entries(catalogoProdutos)) {
    if (id > 5) {
        break
    }
    const li = document.createElement('li');
    li.className = 'produto';
    li.innerHTML = `
            <img src="${produto.imagem}" alt="${produto.nome}" />
            <div class="descricao">
                <h3>${produto.nome}</h3>
                <p>${produto.descricao}</p>
                <p>R$ ${produto.preco.toFixed(2).replace('.', ',')}</p>
                <button class="add-Carrinho" onclick="adicionarAoCarrinho('${parseInt(produto.id)}', event)">Adicionar ao Carrinho</button>
            </div>
        `;
    ul.appendChild(li);
}

const produtos = Array.from(ul.children);

produtos.forEach(p => ul.appendChild(p.cloneNode(true)));
produtos.slice().reverse().forEach(p => ul.insertBefore(p.cloneNode(true), ul.firstChild));

const totalProdutos = produtos.length;
const produtoWidth = produtos[0].offsetWidth + 30;
let currentIndex = totalProdutos;

function updatePosition(animate = true) {
    ul.style.transition = animate ? 'transform 0.5s ease' : 'none';
    ul.style.transform = `translateX(${-currentIndex * produtoWidth}px)`;
}

function checkLoop() {
    const total = ul.children.length;
    if (currentIndex <= 0) {
        currentIndex = totalProdutos;
        updatePosition(false);
    } else if (currentIndex >= total - totalProdutos) {
        currentIndex = totalProdutos;
        updatePosition(false);
    }
}

updatePosition(false);

setaDir.addEventListener('click', () => {
    currentIndex++;
    updatePosition();
    setTimeout(checkLoop, 510);
});

setaEsq.addEventListener('click', () => {
    currentIndex--;
    updatePosition();
    setTimeout(checkLoop, 510);
});

document.querySelectorAll('.seta').forEach(seta => {
    seta.addEventListener('mousedown', () => {
        seta.style.transform = 'translateY(-50%) scale(0.9)';
    });
    seta.addEventListener('mouseup', () => {
        seta.style.transform = 'translateY(-50%) scale(1)';
    });
    seta.addEventListener('mouseleave', () => {
        seta.style.transform = 'translateY(-50%) scale(1)';
    });
});

// Carrinho
let carrinhoProdutos = JSON.parse(localStorage.getItem('carrinhoFabidoces')) || [];

const intervaloAutoPlay = 7000;

function moverAutomaticamente() {
    currentIndex++;
    updatePosition();
    setTimeout(checkLoop, 510);
}

let autoplay = setInterval(moverAutomaticamente, intervaloAutoPlay);

ul.addEventListener('mouseenter', () => clearInterval(autoplay));

ul.addEventListener('mouseleave', () => {
    autoplay = setInterval(moverAutomaticamente, intervaloAutoPlay);
});


window.adicionarAoCarrinho = adicionarAoCarrinho;

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