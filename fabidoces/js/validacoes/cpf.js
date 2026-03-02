import { mensagem } from "../util.js";

export function validaCPF(body){
    const cpf = body.cpf.replace(/\D/g, '').trim();
    
    if (cpf.length !== 11){
        mensagem('CPF Inválido','alert')
        return false
    } else if (validaDigitoCpf(cpf) === true){
        return true
    } else{
        return false
    }
}


function validaDigitoCpf(cpf){
// declarações
    let d1 = 0;
    let d2 = 0;

//1º Digito
    let soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(cpf[i]) * ((10) - i);
    }
    const rd1 = soma % 11;

    if (rd1 === 0 || rd1 === 1){
        d1 = 0;
    } else{
        d1 = (11 - rd1)
    }

//2º Digito
    soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(cpf[i]) * ((11) - i);
    }

//Ultimo Step do loop
    soma += (d1 * 2)

    const rd2 = soma % 11

    if (rd2 === 0 || rd2 === 1){
        d2 = 0;
    } else{
        d2 = (11 - rd2)
    }


//Verifica se os dígitos estão corretos.
    if (parseInt(cpf[9]) !== d1 || parseInt(cpf[10]) !== d2 ){
        mensagem('CPF Inválido','alert')
        return false
    }

    return true
}

