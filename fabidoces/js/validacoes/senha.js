import { mensagem } from "../util.js";


export function validaSenha(body){
    const senha = body.senha;
    const confirmaSenha = body.confirmaSenha;

    const regexValidacao = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{4,30}$/

    if (senha !== confirmaSenha){
        mensagem('Senhas não concidem!','alert')
        return  false
    } else if(regexValidacao.test(senha) === false){
        mensagem('Senha insuficiente! Certifique-se de incluir caractéres especiais, números, letras maiúsculas e minúsculas! ','alert')
        return  false
    }else {
        return true
    }
}