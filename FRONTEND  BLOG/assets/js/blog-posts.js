$(document).ready(function () {

	var nombreAutor = url('?autor');
	var postId = url('?id');
	var paginaId = url('?pid');
	
	if (typeof nombreAutor != 'undefined') {
		$.getJSON('http://localhost:4567/posts-autor/' + nombreAutor, function (arrayOfPosts) {
			paintPosts(arrayOfPosts);	
		}).fail(function() {
		    console.log("error al consumir el servicio posts-autor...");
		});
	}
	
	if (typeof postId != 'undefined') {
		$.getJSON('http://localhost:4567/post-id/' + postId, function (arrayOfPosts) {
			paintPosts(arrayOfPosts);
		}).fail(function() {
		    console.log("error al consumir el servicio post-id...");
		});
	}
	
	if (typeof paginaId != 'undefined') {
		$.getJSON('http://localhost:4567/pagina-id/' + paginaId, function (arrayOfPaginas) {
			var articulos = '';
			$.each(arrayOfPaginas, function(i, obj) {
				articulos += '<section><header class="main">';
				articulos += '<h1>' + obj.titulo + '</h1></header>';
				articulos += '<p>' + obj.texto + '</p>';
				articulos += '<h4>Autores</h4>';
				articulos += '<p>' + obj.autor + '</p>';
				articulos += '</section>'
			});
			$('#articulos').replaceWith(articulos);
		}).fail(function() {
		    console.log("error al consumir el servicio pagina-id...");
		});
	}
	
	function paintPosts(arrayOfPosts) {
		var articulos = '';
		$.each(arrayOfPosts, function(i, obj) {
			articulos += '<section><header class="main">';
			articulos += '<h1>' + obj.titulo + '</h1></header><h3>Resumen</h3><div class="box">';
			articulos += '<p>' + obj.resumen + '</p></div>';
			articulos += '<p>' + obj.texto + '</p><h4>Links Relacionados</h4><ul class="alt">';
			$.each(obj['links-relacionados'], function(i2, link) {
				articulos += '<li>' + link + '</li>';
			});
			articulos += '</ul><h4>Tags</h4>';
			articulos += '<p>' + obj.tags + '</p>';
			articulos += '<h4>Autores</h4>';
			articulos += '<p>' + obj.autor + '</p>';
			articulos += '</section>'
		});
		$('#articulos').replaceWith(articulos);
	}
});
