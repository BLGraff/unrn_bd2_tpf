$(document).ready(function () {
	var q = url('?q');
	$.getJSON('http://localhost:4567/search/' + q, function (arrayOfPosts) {
		console.log(arrayOfPosts);
		var articulo = '';
		$.each(arrayOfPosts, function(i, obj) {
			articulo += '<tr>';
			articulo += '<td>' + obj.autor + '</td>';
			articulo += '<td><a href=posts.html?id=' + obj._id.$oid + '>' + obj.titulo + '</a></td>';
			articulo += '<td>' + obj.resumen + '</td>';
			articulo += '<td>' + obj.fecha.$date + '</td>';
			articulo += '</tr>';
		});
		$('#resultados').append(articulo);

	}).fail(function() {
	    console.log("error al consumir el servicio de search...");
	});
});
