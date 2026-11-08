/*
 * Filtro das tabelas: <input data-filter-table="idDaTabela">.
 * Esconde as linhas que não contêm o texto digitado (sem acento e sem diferenciar maiúsculas).
 * As listagens do backend ainda não são paginadas, então o filtro é feito no navegador.
 */
(function () {
    function normalize(text) {
        return text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    }

    document.querySelectorAll('input[data-filter-table]').forEach(function (input) {
        var table = document.getElementById(input.getAttribute('data-filter-table'));
        if (!table) return;
        input.disabled = false;
        input.addEventListener('input', function () {
            var term = normalize(input.value.trim());
            table.querySelectorAll('tbody tr').forEach(function (row) {
                row.style.display = normalize(row.textContent).indexOf(term) === -1 ? 'none' : '';
            });
        });
    });
})();
