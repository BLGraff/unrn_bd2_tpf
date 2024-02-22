$(document).ready(function () {
	var paginaId = 'tYKPfI0Bz6X9Q11tXgDp';
	
	$.getJSON('http://localhost:4567/pagina-id/' + paginaId, function (arrayOfPaginas) {
		console.log(arrayOfPaginas);
		$.each(arrayOfPaginas, function(i, obj) {
			articulo = '<header><h1>' + obj.titulo + '</h1></header>';
			articulo += '<p>' + obj.texto.substring(0, 100) + '...</p><ul class="actions"><li>';
			articulo += '<a href="posts.html?pid=' + obj._id.$oid + '" class="button big">Leer más...</a>';
			articulo += '</li></ul>';
			$('#paginaPrincipal').append(articulo);
		});
	}).fail(function() {
	    console.log("error al consumir el servicio pagina-id...");
	});
	
	$.getJSON('http://localhost:4567/byautor', function (data) {
		console.log(data);
		var content = '';
		$.each(data, function(i, obj) {
			content += '<li>';
			content += '<a href=posts.html?autor='+ encodeURIComponent(obj._id)+'>' + obj._id + ' (' + obj.count + ')</a>';
			content += '</li>';
		});
		var ul = $('<ul/>').append(content);
		$('#_listactores').replaceWith(ul);
	}).fail(function() {
	    console.log("error al consumir el servicio byauthor...");
	});
	
	$.getJSON('http://localhost:4567/ultimos4posts', function (data) {
		console.log(data);
		var content = '';
		$.each(data, function(i, obj) {
			articulo = '<h3>' + obj.titulo + '</h3>';
			articulo += '<p>' + obj.resumen + '</p><ul class="actions"><li>';
			articulo += '<a href="posts.html?id=' + obj._id.$oid + '" class="button">Leer post...</a>';
			articulo += '</li></ul>';
			var articuloId = 'article' + (i + 1);
			$('#'+articuloId).append(articulo);
		});
	}).fail(function() {
	    console.log("error al consumir el servicio ultimos4posts...");
	});
});
