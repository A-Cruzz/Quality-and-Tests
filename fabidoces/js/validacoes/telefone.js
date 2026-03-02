export function validarTelefone(telefone) {
    // Remove tudo que não é dígito
    const numeroLimpo = telefone.replace(/\D/g, '');

    // Verifica se tem a quantidade mínima de dígitos (10 = fixo com DDD, 11 = celular com DDD)
    if (numeroLimpo.length < 10 || numeroLimpo.length > 11) return false;

    // Validação para celular (começa com 9 após o DDD)
    if (numeroLimpo.length === 11 && !/^[1-9]{2}9[0-9]{8}$/.test(numeroLimpo)) {
        return false;
    }

    return true;
}

export function limparTelefone(telefone){
    let numeroLimpo
    return numeroLimpo = telefone.replace(/\D/g, '');
}