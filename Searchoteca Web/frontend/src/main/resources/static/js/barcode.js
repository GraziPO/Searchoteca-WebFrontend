/*
 * Desenha o código de barras de cada <svg class="barcode" data-code="...">.
 * Usa a biblioteca JsBarcode (Code 128, aceita letras e números).
 * Sem internet o script não carrega, e a etiqueta continua mostrando o código em texto.
 */
(function () {
    if (!window.JsBarcode) return;
    document.querySelectorAll('svg.barcode[data-code]').forEach(function (svg) {
        try {
            JsBarcode(svg, svg.getAttribute('data-code'), {
                format: 'CODE128', height: 46, width: 1.6, fontSize: 12, margin: 0, displayValue: true
            });
        } catch (e) {
            svg.style.display = 'none';
        }
    });
})();
