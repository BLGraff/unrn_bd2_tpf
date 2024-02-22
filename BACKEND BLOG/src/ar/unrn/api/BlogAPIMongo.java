package ar.unrn.api;

import static spark.Spark.get;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.naming.directory.SearchResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import spark.Spark;

public class BlogAPIMongo {

	public static void main(String[] args) {

		/**
		 * Recupera una página por su id.
		 */
		get("/pagina-id/:id", (req, res) -> {
			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {

				res.header("Access-Control-Allow-Origin", "*");
				// Recupero el id que viene por parámetro
				String paginaId = req.params("id");

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("pagina");

				// collection.find(Filters.eq("_id", new ObjectId(paginaId))).forEach((Document
				// d) -> System.out.println(d));
				FindIterable<Document> documento = collection.find(Filters.eq("_id", new ObjectId(paginaId)));

				String result = StreamSupport.stream(documento.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				return result;
			}

		});

		/**
		 * Devuelve un array de objetos id,count. Donde id es el nombre del autor y
		 * count la cantidad de post que realizó.
		 */
		get("/byautor", (req, res) -> {
			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
				res.header("Access-Control-Allow-Origin", "*");

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("post");

				AggregateIterable<Document> documento = collection.aggregate(Arrays.asList(
						// Aggregates.match(Filters.eq("categories", "Bakery")),
						// Aggregates.unwind("miAutor"),
						Aggregates.group("$autor", Accumulators.sum("count", 1))));

				String result = StreamSupport.stream(documento.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				return result;

			}
		});

		/**
		 * Retorna los ultimos 4 post ordenados por fecha.
		 */
		get("/ultimos4posts", (req, res) -> {
			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
				res.header("Access-Control-Allow-Origin", "*");

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("post");

				FindIterable<Document> ds = collection.find()
						.projection(Projections.include("_id", "titulo", "resumen")).limit(4)
						.sort(Sorts.descending("fecha"));

				String result = StreamSupport.stream(ds.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				return result;
			}
		});

		/**
		 * Retorna todos los Post para un autor, dado su nombre Debe retornar un json
		 * con la siguiente estructura:
		 */
		get("/posts-autor/:nombreautor", (req, res) -> {

			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {

				res.header("Access-Control-Allow-Origin", "*");
				// Recupero el nombre del autor que viene como parámetro
				String nombreAutor = req.params("nombreautor");

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("post");

				FindIterable<Document> documento = collection.find(Filters.eq("autor", nombreAutor));

				String result = StreamSupport.stream(documento.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				return result;
			}

		});

		/**
		 * Retorna un post dado un id.
		 */
		get("/post-id/:id", (req, res) -> {

			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {

				res.header("Access-Control-Allow-Origin", "*");
				// Recupero el id del post que viene por parámetro
				String postId = req.params("id");

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("post");

				FindIterable<Document> documento = collection.find(Filters.eq("_id", new ObjectId(postId)));

				String result = StreamSupport.stream(documento.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				return result;
			}

		});

		/**
		 * Búsqueda libre dentro del texto del documento.
		 */
		get("/search/:text", (req, res) -> {
			try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {

				res.header("Access-Control-Allow-Origin", "*");
				// Recupero la palabra/frase ingresada por el usuario
				String text = req.params("text");
				//System.out.println("busco: "+text);

				// Obtengo la BD y la Colección
				MongoDatabase database = mongoClient.getDatabase("BlogBDII");
				MongoCollection<Document> collection = database.getCollection("post");

				//creo el indice
				collection.createIndex(Indexes.text("texto"));
				
				FindIterable<Document> documento = collection.find(Filters.text(text));
				
				String result = StreamSupport.stream(documento.spliterator(), false).map(Document::toJson)
						.collect(Collectors.joining(", ", "[", "]"));

				//System.out.println(result);
				return result;
			}

		});

		Spark.exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});

	}
}