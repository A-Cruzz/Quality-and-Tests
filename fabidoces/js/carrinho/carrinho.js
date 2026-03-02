import { catalogo } from "../cardapio/cardapio.js";
import { validarEmail } from "../validacoes/email.js"
import { validarTelefone, limparTelefone } from "../validacoes/telefone.js";


//Obetenção do conteúdo do carrinho e separação em array.
let carrinhoProdutos = JSON.parse(localStorage.getItem('carrinhoFabidoces')) || [];
let totalItens
let pedido;

function atualizarTotalItens() {
    totalItens = carrinhoProdutos.reduce((total, item) => total + item.quantidade, 0);
    //document.getElementById('contador-carrinho').textContent = totalItens;
}

function salvarCarrinho() {
    localStorage.setItem('carrinhoFabidoces', JSON.stringify(carrinhoProdutos));
    atualizarTotalItens();
}

function carregarCarrinho() {
    const dadosSalvos = localStorage.getItem('carrinhoFabidoces');
    if (dadosSalvos) {
        carrinhoProdutos = JSON.parse(dadosSalvos);
    } else {
        carrinhoProdutos = [];
    }
}

export async function adicionarAoCarrinho(id, event) {
    if (catalogo.length === 0) {
        await import("../cardapio/cardapio.js").then(module => {
            catalogo = module.catalogo;
        });
    }

    const produto = catalogo.find(item => item.id === parseInt(id));
    if (!produto) {
        console.error('Produto não existe:', id);
        return;
    }

    const itemExistente = carrinhoProdutos.find(item => item.id === id);

    if (itemExistente) {
        itemExistente.quantidade++;
    } else {
        carrinhoProdutos.push({
            id: produto.id,
            nome: produto.nome,
            preco: produto.preco,
            imagem: produto.imagem,
            quantidade: 1
        });
    }

    if (event) {
        const botao = event.target;
        botao.textContent = '✔ Adicionado ao Carrinho!';
        botao.disabled = true;

        setTimeout(() => {
            botao.textContent = 'Adicionar ao Carrinho';
            botao.disabled = false;
        }, 1500);
    }
    salvarCarrinho()
    renderCarrinho()
}

function removerProduto(id) {
    id = parseInt(id);
    carrinhoProdutos = carrinhoProdutos.filter(item => {
        const manter = item.id !== id;
        if (!manter) { }
        return manter;
    });
    salvarCarrinho();
    renderCarrinho();
}

function alterarQuantidade(id, delta) {
    const index = carrinhoProdutos.findIndex(item => item.id == id);
    if (index === -1) return;

    carrinhoProdutos[index].quantidade += delta;

    if (carrinhoProdutos[index].quantidade <= 0) {
        removerProduto(id);
    }
    salvarCarrinho();
    renderCarrinho();
}

function atualizarQuantidadeDiretamente(id, novaQuantidade) {
    const quantidade = parseInt(novaQuantidade);
    if (isNaN(quantidade)) return;

    const index = carrinhoProdutos.findIndex(item => item.id == id);
    if (index === -1) return;

    if (quantidade <= 0) {
        removerProduto(id);
    } else {
        carrinhoProdutos[index].quantidade = quantidade;
    }

    salvarCarrinho();
    renderCarrinho();
}

function renderCarrinho() {
    const carrinho = document.getElementById("carrinho");
    const valorTotal = document.getElementById("valor-total");

    // Verifica se o carrinho está vazio
    if (!carrinhoProdutos || carrinhoProdutos.length === 0) {
        if (carrinho) {
            carrinho.innerHTML = `
                <div class="carrinho-vazio">
                    <p>Seu carrinho está vazio. Adicione alguns itens para continuar sua compra! <a href="cardapio.html">Ver Cardápio</a></p>
                </div>
            `;
        }
        if (valorTotal) {
            valorTotal.textContent = `R$ 0,00`;
        }
        return;
    }

    if (carrinho) {
        carrinho.innerHTML = "";
        let total = 0;

        carrinhoProdutos.forEach(item => {
            total += item.quantidade * item.preco;

            const produto = document.createElement("div");
            produto.className = "produto-doce";

            produto.innerHTML = `
            <div class="imagem">
              <img src="${item.imagem}" alt="${item.nome}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 10px;" />
            </div>
            <div class="info">
              <p>${item.nome}</p>
              <div class="quantidade-controle">
                <button onclick="window.alterarQuantidade('${item.id}', -1)">−</button>
                <input type="number" value="${item.quantidade}" min="1" 
                       onchange="window.atualizarQuantidadeDiretamente('${item.id}', this.value)">
                <button onclick="window.alterarQuantidade('${item.id}', 1)">+</button>
              </div>
              <p>Subtotal: R$ ${(item.preco * item.quantidade).toFixed(2).replace('.', ',')}</p>
              <button class="botao remover" onclick="window.removerProduto('${item.id}')">Remover</button>
            </div>
          `;

            carrinho.appendChild(produto);
        });

        if (valorTotal) {
            valorTotal.textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
        }
    }
}

function criarPopupConfirmacao() {
    let popup = document.getElementById('popup-confirmacao');
    if (popup) return popup;

    // Cria o pop-up
    popup = document.createElement('div');
    popup.id = 'popup-confirmacao';
    popup.className = 'popup-container';
    popup.style.display = 'none';

    popup.innerHTML = `
        <div class="popup-box">
            <div class="popup-header">
                <h3>Confirme seu pedido</h3>
                <span class="popup-close">&times;</span>
            </div>
            <div class="popup-body">
                <div id="popup-resumo" class="popup-resumo"></div>
                <div class="popup-total">
                    <strong>Total:</strong> <span id="popup-total">R$ 0,00</span>
                </div>
            </div>
            <div class="popup-footer">
                <button class="popup-btn popup-cancelar">Cancelar</button>
                <button id="limiteClique" class="popup-btn popup-confirmar">Pagamento</button>
            </div>
        </div>
    `;

    document.body.appendChild(popup);

    popup.querySelector('.popup-close').addEventListener('click', fecharPopup);
    popup.querySelector('.popup-cancelar').addEventListener('click', fecharPopup);
    
    const botaoPagamento = popup.querySelector('.popup-confirmar'); 
    
    botaoPagamento.addEventListener('click', function() {
        finalizarCompra(); 
        this.disabled = true;
        this.textContent = 'Processando'
    });

    return popup;
}

function criarPopupConfirmacaoReserva() {
    let popup = document.getElementById('popup-confirmacao');
    if (popup) return popup;

    // Cria o pop-up
    popup = document.createElement('div');
    popup.id = 'popup-confirmacao';
    popup.className = 'popup-container';
    popup.style.display = 'none';

    popup.innerHTML = `
        <div class="popup-box">
            <div class="popup-header">
                <h3>Confirme seu pedido</h3>
                <span class="popup-close">&times;</span>
            </div>
            <div class="popup-body">
                <div id="popup-resumo" class="popup-resumo"></div>
                <div class="popup-total">
                    <strong>Total:</strong> <span id="popup-total">R$ 0,00</span>
                </div>
            </div>
            <div class="popup-footer">
                <button class="popup-btn popup-cancelar">Cancelar</button>
                <button id="limiteClique" class="popup-btn popup-confirmar">Reservar</button>
            </div>
        </div>
    `;

    document.body.appendChild(popup);

    popup.querySelector('.popup-close').addEventListener('click', fecharPopup);
    popup.querySelector('.popup-cancelar').addEventListener('click', fecharPopup);
    
    const botaoPagamento = popup.querySelector('.popup-confirmar'); 
    
    botaoPagamento.addEventListener('click', function() {
        finalizarReserva(); 
        this.disabled = true;
        this.textContent = 'Processando'
    });

    return popup;
}

function criarPopUpEscolha() {
    // Validações antes de mostrar o popup
    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const telefone = document.getElementById('telefone').value;

    if (carrinhoProdutos.length === 0) {
        alert('Seu carrinho está vazio!');
        return;
    }

    if (!nome || !email || !telefone) {
        alert('Por gentileza, preencha seus dados antes de prosseguirmos.');
        return;
    }

    if (!validarEmail(email)) {
        alert('Insira um email válido');
        return;
    }

    if (!validarTelefone(telefone)) {
        alert('Insira um telefone válido');
        return;
    }

    const popupId = 'popup-escolha-detalhada';
    let popup = document.getElementById(popupId);
    
    if (popup) {
        popup.style.display = 'flex';
        return popup;
    }

    popup = document.createElement('div');
    popup.id = popupId;
    popup.className = 'popup-container';
    popup.style.display = 'none';

    popup.innerHTML = `
        <div class="popup-box">
            <div class="popup-header">
                <h3>Como deseja prosseguir?</h3>
                <span class="popup-close">&times;</span>
            </div>
            <div class="popup-body">
                <p>Por favor, selecione uma das opções abaixo:</p>
            </div>
            <div class="popup-footer footer-escolha-detalhada">
                <button id="escolhaBotaoOpcao1" class="popup-btn">
                    <p style="margin: 0; text-align: left; font-weight: normal;">
                        <strong>Confirmar Pedido:</strong>
                        <br/>Ao escolher esta opção, você irá confirmar o pedido e prosseguir para o pagamento (pix).
                    </p>
                </button>
                <button id="escolhaBotaoOpcao2" class="popup-btn">
                    <p style="margin: 0; text-align: left; font-weight: normal;">
                        <strong>Reservar:</strong>
                        <br/>Ao escolher esta opção, você irá reservar o pedido e manterá contato com a loja para combinar o pagamento.
                    </p>
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(popup);

    // Configuração de eventos
    popup.querySelector('.popup-close').addEventListener('click', () => {
        popup.style.display = 'none';
    });

    popup.querySelector('#escolhaBotaoOpcao1').addEventListener('click', function() {
        popup.style.display = 'none';
        mostrarPopupConfirmacao();
    });

    popup.querySelector('#escolhaBotaoOpcao2').addEventListener('click', function() {
        popup.style.display = 'none';
        mostrarPopupReserva();        
    });

    popup.style.display = 'flex';
    return popup;
}


export function mostrarPopupConfirmacao() {
    //Validações
    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const telefone = document.getElementById('telefone').value

    if (carrinhoProdutos.length === 0) {
        alert('Seu carrinho está vazio!');
        return;
    }

    if (!nome || !email || !telefone) {
        alert('Por gentileza, preencha seus dados antes de prosseguirmos.')
        return;
    }

    if (!validarEmail(email)) {
        alert('Insira um email válido')
        return;
    }

    if (!validarTelefone(telefone)) {
        alert('Insira um telefone válido')
        return
    }

    const popup = criarPopupConfirmacao();
    const resumo = document.getElementById('popup-resumo');
    const total = document.getElementById('popup-total');

    resumo.innerHTML = '';

    carrinhoProdutos.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'popup-item';
        itemDiv.innerHTML = `
            <span>${item.quantidade}x ${item.nome}</span>
            <span>R$ ${(item.preco * item.quantidade).toFixed(2).replace('.', ',')}</span>
        `;
        resumo.appendChild(itemDiv);
    });

    const totalPedido = carrinhoProdutos.reduce((sum, item) => sum + (item.preco * item.quantidade), 0);
    total.textContent = `R$ ${totalPedido.toFixed(2).replace('.', ',')}`;

    popup.style.display = 'flex';
}



export function mostrarPopupReserva() {

    const popup = criarPopupConfirmacaoReserva();
    const resumo = document.getElementById('popup-resumo');
    const total = document.getElementById('popup-total');

    resumo.innerHTML = '';

    carrinhoProdutos.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'popup-item';
        itemDiv.innerHTML = `
            <span>${item.quantidade}x ${item.nome}</span>
            <span>R$ ${(item.preco * item.quantidade).toFixed(2).replace('.', ',')}</span>
        `;
        resumo.appendChild(itemDiv);
    });

    const totalPedido = carrinhoProdutos.reduce((sum, item) => sum + (item.preco * item.quantidade), 0);
    total.textContent = `R$ ${totalPedido.toFixed(2).replace('.', ',')}`;

    popup.style.display = 'flex';
}


function fecharPopup() {
    const popup = document.getElementById('popup-confirmacao');
    if (popup) {
        popup.style.display = 'none';
    }
}

async function finalizarCompra() {
    if (carrinhoProdutos.length === 0) return;

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const telefone = limparTelefone(document.getElementById('telefone').value)

    pedido = {
        datetime: new Date().toLocaleString('pt-BR'),
        items: carrinhoProdutos.map(item => ({
            name: item.nome,
            quantity: item.quantidade,
            price: item.preco,
            image: item.imagem
        })),
        client: {
            name: nome,
            email: email,
            tel: telefone
        },
        price: carrinhoProdutos.reduce((sum, item) => sum + (item.preco * item.quantidade), 0)
    };

    try {
        const response = await fetch("http://localhost:8080/api/payment/pix", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(pedido)
        });
        const pixData = await response.json();
        mostrarQRCode(pixData);
        limpaFormulario();

    } catch (error) {
        console.error("Erro ao buscar dados do PIX:", error);
    }

    //Pós Pedido.
    carrinhoProdutos = [];
    localStorage.removeItem('carrinhoFabidoces');
    renderCarrinho();
    fecharPopup();

}

async function finalizarReserva() {
    if (carrinhoProdutos.length === 0) return;

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const telefone = limparTelefone(document.getElementById('telefone').value)

    pedido = {
        datetime: new Date().toLocaleString('pt-BR'),
        items: carrinhoProdutos.map(item => ({
            name: item.nome,
            quantity: item.quantidade,
            price: item.preco,
            image: item.imagem
        })),
        client: {
            name: nome,
            email: email,
            tel: telefone
        },
        price: carrinhoProdutos.reduce((sum, item) => sum + (item.preco * item.quantidade), 0)
    };

    try {
        const response = await fetch("http://localhost:8080/api/payment/reserve", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(pedido)
        });
        const httpResponse = await response.json();
        if (httpResponse){
            limpaFormulario();
        } else{
            alert('Não foi possível enviar seu pedido, ocorreu um erro interno, por gentileza tente novamente mais tarde.')
        }
        

    } catch (error) {
        console.error("Erro ao enviar a requisição:", error);
    }

    //Pós Pedido.
    carrinhoProdutos = [];
    localStorage.removeItem('carrinhoFabidoces');
    renderCarrinho();
    fecharPopup();
    alert("Pedido reservado! Enviamos um email com as informações do seu pedido. Entraremos em contato para combinar o pagamento.");
}


function mostrarQRCode(pixData) {
    let qrContainer = document.getElementById('qr-code-container');
    if (!qrContainer) {
        qrContainer = document.createElement('div');
        qrContainer.id = 'qr-code-container';
        qrContainer.className = 'qr-code-overlay';
        qrContainer.innerHTML = `
            <div class="qr-code-box">
                <h3>Pagamento PIX</h3>
                <canvas id="qrcode"></canvas>
                <p>Valor: R$ ${pixData.amount.toFixed(2)}</p>
                <p>Expira em: ${new Date(pixData.expirationTime).toLocaleString()}</p>
                <p class="instrucoes-pix">Copie o código abaixo:</p>
                <div class="codigo-pix-container">
                    <p id="qr-code-text" class="codigo-pix">${pixData.qrCode}</p>
                    <button id="copiar-pix" class="botao-copiar">
                        <i class="fas fa-copy"></i>
                    </button>
                </div>
                <button onclick="fecharQRCode()" class="botao">Fechar</button>
            </div>
        `;
        document.body.appendChild(qrContainer);

        document.getElementById('copiar-pix').addEventListener('click', copiarCodigoPix);
    }

    // Renderiza o QR Code usando a biblioteca
    const canvas = document.getElementById("qrcode");
    QRCode.toCanvas(canvas, pixData.qrCode, { width: 250 });

    qrContainer.style.display = 'flex';

    function copiarCodigoPix() {
        navigator.clipboard.writeText(pixData.qrCode).then(() => {
            const btnCopiar = document.getElementById('copiar-pix');
            btnCopiar.innerHTML = '<i class="fas fa-check"></i>';
            setTimeout(() => {
                btnCopiar.innerHTML = '<i class="fas fa-copy"></i>';
            }, 2000);
        });
    }
}

export function fecharQRCode() {
    const qrContainer = document.getElementById('qr-code-container');
    if (qrContainer) {
        qrContainer.style.display = 'none';
    }
}


//Formatação do campo de telefone - Exclusivo página de carrinho**
if (document.getElementById("telefone")) {
    document.getElementById("telefone").addEventListener("input", function (event) {
        let input = event.target;
        let value = input.value.replace(/\D/g, "");

        if (value.length > 2) {
            value = `(${value.substring(0, 2)}) ${value.substring(2)}`;
        }
        if (value.length > 10) {
            value = `${value.substring(0, 10)}-${value.substring(10, 14)}`;
        }

        input.value = value;
    });
}

function limpaFormulario() {
    const ids = ['nome', 'email', 'telefone'];
    ids.forEach(id => {
        const campo = document.getElementById(id);
        if (campo) campo.value = '';
    });
    if (ids.length > 0) document.getElementById(ids[0]).focus();
}


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


// Inicializaçao

window.alterarQuantidade = alterarQuantidade;
window.atualizarQuantidadeDiretamente = atualizarQuantidadeDiretamente;
window.removerProduto = removerProduto;
window.mostrarPopupConfirmacao = mostrarPopupConfirmacao;
window.fecharQRCode = fecharQRCode;
window.criarPopUpEscolha = criarPopUpEscolha;

carregarCarrinho();
renderCarrinho();