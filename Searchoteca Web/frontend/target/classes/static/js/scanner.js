/*
 * Contador da leitura em lote: <textarea data-scan-count="idDoContador">.
 * O leitor de código de barras USB digita o código e aperta Enter, então cada linha é um exemplar.
 * Mostra quantos códigos foram lidos e avisa quando há repetidos.
 */
(function () {
    document.querySelectorAll('textarea[data-scan-count]').forEach(function (area) {
        var counter = document.getElementById(area.getAttribute('data-scan-count'));
        if (!counter) return;
        function update() {
            var codes = area.value.split(/[\s,;]+/).filter(function (c) { return c.length > 0; });
            var unique = new Set(codes);
            var text = codes.length + (codes.length === 1 ? ' código lido' : ' códigos lidos');
            if (unique.size !== codes.length) {
                text += ' — ' + (codes.length - unique.size) + ' repetido(s)';
                counter.classList.add('warn');
            } else {
                counter.classList.remove('warn');
            }
            counter.textContent = text;
        }
        area.addEventListener('input', update);
        update();
    });
})();
