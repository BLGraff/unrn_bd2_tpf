package ar.unrn.api;

import static spark.Spark.get;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpHost;
import org.bson.Document;
import org.elasticsearch.client.RestClient;

import ar.unrn.modelo.Pagina;
import ar.unrn.modelo.Post;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class BlogAPIElasticsearch {

	public static void main(String[] args) {
		RestClient restClient = RestClient.builder(HttpHost.create("http://localhost:9200")).build();

		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		ElasticsearchClient esClient = new ElasticsearchClient(transport);

		inicializarPaginas(esClient);
		inicializarPosts(esClient);

		/**
		 * Recupera una página por su id.
		 * https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/reading.html
		 */
		get("/pagina-id/:id", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");

				// Recupero el id que viene por parámetro
				String paginaId = req.params("id");

				// Busco el documento
				GetResponse<Pagina> response = esClient.get(g -> g.index("pagina").id(paginaId), Pagina.class);

				if (!response.found()) {
					return null;
				}

				Pagina pag = response.source();
				JsonArray json = Json.createArrayBuilder().add(Json.createObjectBuilder()
						.add("_id", Json.createObjectBuilder().add("$oid", response.id()))
						.add("titulo", pag.getTitulo()).add("texto", pag.getTexto()).add("autor", pag.getAutor())
						.add("fecha", Json.createObjectBuilder().add("$date", pag.getFecha()))).build();

				return json;

			} catch (Exception e) {
				throw e;
			}

		});

		/**
		 * Retorna los ultimos 4 post ordenados por fecha.
		 */
		get("/ultimos4posts", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");

				SearchResponse<Post> response = esClient.search(s -> s.index("post").size(4) // limit 4
						.sort(so -> so // order by
								.field(FieldSort.of(f -> f.field("fecha").order(SortOrder.Desc)))),
						Post.class);

				List<Hit<Post>> hits = response.hits().hits();

				JsonArrayBuilder jsonArray = Json.createArrayBuilder();

				for (Hit<Post> hit : hits) {
					Post post = hit.source();

					JsonObject json = Json.createObjectBuilder()
							.add("_id", Json.createObjectBuilder().add("$oid", hit.id()))
							.add("titulo", post.getTitulo()).add("resumen", post.getTexto()).build();

					jsonArray.add(json);
				}

				return jsonArray.build();
			} catch (Exception e) {
				throw e;
			}

		});

		/**
		 * Retorna un post dado un id.
		 */
		get("/post-id/:id", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");
				// Recupero el id del post que viene por parámetro
				String postId = req.params("id");

				// Busco el post
				GetResponse<Post> response = esClient.get(g -> g.index("post").id(postId), Post.class);

				if (!response.found()) {
					return null;
				}

				Post post = response.source();

				JsonArrayBuilder links = Json.createArrayBuilder();

				for (String link : post.getLinks_relacionados()) {
					links.add(link);
				}

				JsonArray json = Json.createArrayBuilder().add(Json.createObjectBuilder()
						.add("_id", Json.createObjectBuilder().add("$oid", response.id()))
						.add("titulo", post.getTitulo()).add("resumen", post.getTitulo()).add("texto", post.getTexto())
						.add("tags", post.getTag()).add("links-relacionados", links).add("autor", post.getAutor())
						.add("fecha", Json.createObjectBuilder().add("$date", post.getFecha()))).build();

				return json;

			} catch (Exception e) {
				throw e;
			}

		});

		/**
		 * Devuelve un array de objetos id,count. Donde id es el nombre del autor y
		 * count la cantidad de post que realizó.
		 */
		get("/byautor", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");

				SearchResponse<Post> response = esClient.search(s -> s.index("post").size(0) // count
						.aggregations("agrupar_por_autor", a -> a // group by
								.terms(t -> t.field("autor.keyword"))),
						Post.class);

				List<StringTermsBucket> buckets = response.aggregations().get("agrupar_por_autor").sterms().buckets()
						.array();

				JsonArrayBuilder jsonArray = Json.createArrayBuilder();

				for (StringTermsBucket bucket : buckets) {
					JsonObject json = Json.createObjectBuilder().add("_id", bucket.key().stringValue())
							.add("count", bucket.docCount()).build();
					jsonArray.add(json);
				}

				return jsonArray.build();
			} catch (Exception e) {
				throw e;
			}
		});

		/**
		 * Retorna todos los Post para un autor
		 */
		get("/posts-autor/:nombreautor", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");
				// Recupero el nombre del autor que viene como parámetro
				String nombreAutor = req.params("nombreautor");

				// Busco el documento
				SearchResponse<Post> response = esClient.search(
						s -> s.index("post").query(q -> q.match(t -> t.field("autor").query(nombreAutor))), Post.class);

				List<Hit<Post>> hits = response.hits().hits();

				JsonArrayBuilder jsonArray = Json.createArrayBuilder();

				for (Hit<Post> hit : hits) {
					Post post = hit.source();

					JsonArrayBuilder links = Json.createArrayBuilder();

					for (String link : post.getLinks_relacionados()) {
						links.add(link);
					}

					JsonObject json = Json.createObjectBuilder()
							.add("_id", Json.createObjectBuilder().add("$oid", hit.id()))
							.add("titulo", post.getTitulo()).add("resumen", post.getTitulo())
							.add("texto", post.getTexto()).add("tags", post.getTag()).add("links-relacionados", links)
							.add("autor", post.getAutor())
							.add("fecha", Json.createObjectBuilder().add("$date", post.getFecha())).build();

					jsonArray.add(json);
				}

				return jsonArray.build();

			} catch (Exception e) {
				throw e;
			}
		});

		/**
		 * Búsqueda libre sobre el texto de los posts.
		 */
		get("/search/:text", (req, res) -> {
			try {
				res.header("Access-Control-Allow-Origin", "*");
				// Recupero la palabra/frase ingresada por el usuario
				String text = req.params("text");

				// Busco el documento
				SearchResponse<Post> response = esClient.search(s -> s
						.index("post")
						.query(q -> q
								.match(t -> t
										.field("texto")
										.query('+'+text+'+')
								)
						), Post.class);

				List<Hit<Post>> hits = response.hits().hits();

				JsonArrayBuilder jsonArray = Json.createArrayBuilder();

				for (Hit<Post> hit : hits) {
					Post post = hit.source();

					JsonObject json = Json.createObjectBuilder()
							.add("_id", Json.createObjectBuilder().add("$oid", hit.id()))
							.add("titulo", post.getTitulo())
							.add("resumen", post.getTexto())
							.add("autor", post.getAutor())
							.add("fecha", Json.createObjectBuilder().add("$date", post.getFecha())).build();

					jsonArray.add(json);
				}

				return jsonArray.build();
				
			} catch (Exception e) {
				throw e;
			}
		});

	}

	private static void inicializarPaginas(ElasticsearchClient esClient) {
		// creo indice pagina
		try {
			esClient.indices().delete(d -> d.index("pagina"));

			esClient.indices().create(c -> c.index("pagina"));

			// agrego pagina1
			String titulo = "Bienvenido";
			String texto = "Bienvenido este es un blog de donde podrás encontrar información relacionada a la programación";
			String autor = "Graff Braian Lisandro";

			// DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String fecha = "2020-10-17T19:58:48.408Z";
			// Date fecha = df2.parse(string2);

			Pagina pagina1 = new Pagina(titulo, texto, autor, fecha);

			IndexResponse response1 = esClient.index(i -> i.index("pagina")
					// .id(pagina1.getId())
					.document(pagina1));

			// agrego pagina2
			titulo = "Ya están disponibles las entradas para nerdear.la 2020";
			texto = "En nerdear.la la idea es aprender, divertirse y conocer gente como vos. "
					+ "Es un evento de cinco días en donde vas a poder participar de charlas y talleres, "
					+ "compartir en qué estás trabajando, conocer gente fantástica, disfrutar juegos ¡y todo 100% gratis!";
			autor = "https://nerdear.la/";

			fecha = "2020-10-20T19:58:48.408Z";
			// fecha = df2.parse(string2);

			Pagina pagina2 = new Pagina(titulo, texto, autor, fecha);

			IndexResponse response2 = esClient.index(i -> i.index("pagina").id("tYKPfI0Bz6X9Q11tXgDp") // lo pongo a
																										// mano porque
																										// el blog tiene
																										// hardcodeado
																										// un id
					.document(pagina2));

		} catch (ElasticsearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
			 * catch (ParseException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

	}

	private static void inicializarPosts(ElasticsearchClient esClient) {

		// creo indice post
		try {

			esClient.indices().delete(d -> d.index("post"));

			esClient.indices().create(c -> c.index("post"));

			// agrego post1
			String titulo = "Stop using isLoading booleans";
			String texto = "Vamos a jugar un poco con la API de geolocalización un poco y aprender acerca de "
					+ "los peligros de isLoading booleanos (y booleanos similares como: isRejected, isIdleo isResolved), "
					+ "mientras que estamos en él. Usaré React para demostrar esto, pero los conceptos se aplican generalmente a cualquier marco o lenguaje.";
			String tag = "isLoading";
			String[] links_relacionados = new String[] { "https://kentcdodds.com/blog/stop-using-isloading-booleans",
					"https://egghead.io/lessons/react-use-a-status-enum-instead-of-booleans",
					"https://egghead.io/lessons/react-handle-http-errors-with-react", };
			String autor = "Kent C. Dodds";

			// DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String fecha = "2020-03-02T23:10:36.408Z";
			// Date fecha = df2.parse(string2);

			Post post1 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response1 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post1));

			// agrego post2
			titulo = "Verifying an Email Address Without Sending an Email in NodeJS";
			texto = "Casi todas las plataformas en Internet deben poder identificar de manera única a sus usuarios, y las direcciones de correo electrónico son el mecanismo más común para lograrlo. Sin embargo, están plagados de todo tipo de problemas, desde cuentas de spam hasta troles descarados, perpetrados por bots y otros actores maliciosos. Todas las entradas proporcionadas por el usuario deben validarse antes de guardarse en la base de datos, y doblemente para las direcciones de correo electrónico, considerando el importante papel que desempeñan. La mayoría de los sistemas implementan esta funcionalidad ejecutando una simple verificación de expresiones regulares con la dirección de correo electrónico para la validación de la sintaxis y enviando un correo electrónico a la dirección proporcionada por el usuario. Sin embargo, esto puede no ser suficiente para muchos sistemas.\",\n"
					+ "  \"texto\": \"¿Por qué la validación no es suficiente? La forma más sencilla de eliminar las direcciones de correo electrónico incorrectas es validarlas. La validación solo garantiza que la dirección de correo electrónico proporcionada por el cliente sea sintácticamente correcta. En los términos más estrictos, una dirección de correo electrónico válida debe corresponder a la especificación RFC 2822 . Sin embargo, la mayoría de las aplicaciones pueden salirse con la suya siendo un poco más estrictas de lo que permite la especificación para eliminar a los malos actores. La validación del correo electrónico es un problema relativamente simple. Todo lo que necesita es una simple expresión regular y está bien. Si los requisitos para su aplicación son menos prohibitivos, incluso podría salirse con la suya simplemente detectando si hay un signo '@' en la dirección de correo electrónico proporcionada. La verificación por correo electrónico es un problema diferente por sí solo. Las aplicaciones deben poder verificar las direcciones de correo electrónico para: i) Asegúrese de que exista el correo electrónico. ii) Asegúrese de que el correo electrónico pertenezca a ese usuario en particular. Esta guía se centrará en la primera parte del problema (asegurarse de que exista la dirección de correo electrónico proporcionada). Si su aplicación necesita verificar la propiedad de una dirección de correo electrónico, probablemente no podrá evitar enviar un correo electrónico real a el usuario.";
			tag = "correo";
			links_relacionados = new String[] {
					"https://soshace.com/verifying-an-email-address-without-sending-an-email-in-nodejs/" };
			autor = "Bradley Kofi";

			fecha = "2020-10-21T10:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post2 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response2 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post2));

			// agrego post3
			titulo = "The Difference of “var” vs “let” vs “const” in Javascript";
			texto = "If you saw this article while Googling, I am going to make a wild guess that you are confused about the difference between var, let, and const in Javascript (JS). No worries, I got you, developer. let and const are two new features of ES6, which came out in 2015. Before that, var was the only way to declare a variable. This is one of the things that I’ve been dying to write a blog about because it could be confusing AND fun at the same time!\",\n"
					+ "  \"texto\": \"A quick disclaimer: This is learning progress for me as well, so bear with me if I am not being clear, because I am writing this using my beginner’s perspective. Before we start, I will explain these three variables declaration in three aspects: function-scoped & block-scoped, update & redeclaration, and hoisting to highlight the similarities and differences. Table of Content var let const Conclusion";
			tag = "javascript";
			links_relacionados = new String[] {
					"https://medium.com/@meganslo/the-difference-of-var-vs-let-vs-const-in-javascript-abe37e214d66" };
			autor = "Megan Lo";

			fecha = "2020-10-01T10:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post3 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response3 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post3));

			// agrego post4
			titulo = "React: Thinking in Self Contained Components";
			texto = "This is a post that will get you start with React. It is intended for server-side developers with basic JavaScript or JQuery experience, for Angular developers and for web developers in general without knowledge in React. Basically, that is me, a server-side developer with JQuery and Angular experience. I’m very far to be a React expert. I was curious about React but what makes me to start learning was React Native (build once, in JavaScript/React Design, then, deploy in Android and iOS, real native mobile apps). To begin with, I decided to start with plain React and after that continue with React Native. I have to say that I liked React, a lot! It is a great library created to build real Object Oriented Designs on Web UI. This post is a brief of what I think you have to know to start developing with React. I will start talking a bit about React’s spirit and why it was created. Then I will follow with their few main concepts that you must understand to build applications with React. And finally, I will explain how I built a pretty small React application called react-simple that helped me to put in practice what I have read.\",\n"
					+ "  \"texto\": \"React is a JavaScript library for building user interfaces. It was created by Facebook developers. ¿Why not use one of the many existing JavaScript libraries or frameworks instead of create another one? Well, they think that existing ones are unnecessary complex and doesn’t help with maintainability for big front-end applications (like Facebook). So, they came up with a way to build and communicate self-contained UI components. The first great thing that React gives you is a way to build small loosely coupled components to create User Interfaces. The really (seriously really) hard thing, as always, is Design!. I mean, how you should break a User Interface in small reusable components? There is a nice tutorial that helps specifically on this: Thinking in React. The other great thing that React gives you is the rendering system, which takes care of DOM. You don’t need to manipulate the DOM yourself. React’s rendering system will do that for you thanks to a mechanism called Virtual DOM. When the internal state of a component change, it is re-painted on the screen by the rendering system. Pretty match like an observer pattern (or before that, like the Dependency Mechanism of the MVC implementation that comes with Smalltalk). But in contrast to the Observer Pattern, React’s Components are the observer and the observable, that responsibility is not separated. The Components contain the state and the markup to paint that state on the screen. Do you see how nice is this? This is against many well known development styles where User Interface markup is on a template and gets mixed with state to be painted on the screen. Usually, this is done on Controllers, where state comes from Models and UI from templates Views. Sounds like MVC right? Well, React mindset is pretty different to this.";
			tag = "react";
			links_relacionados = new String[] {
					"http://www.copypasteisforword.com/notes/react-self-contained-components" };
			autor = "Enrique Molinari";

			fecha = "2018-06-08T10:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post4 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response4 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post4));

			// agrego post5
			titulo = "JQueue: A Library to Implement the Outbox Pattern";
			texto = "In microservices or any other event-based architecture, in some use cases, a service might require us to make changes to their own local database and also publish an event. That event is then consumed by other services. To have a consistent software system, it is mandatory that these two actions";
			tag = "Microservices";
			links_relacionados = new String[] { "https://www.copypasteisforword.com/2022-10-23-jqueue/",
					"https://www.copypasteisforword.com/", };
			autor = "Enrique Molinari";

			fecha = "2022-10-23T23:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post5 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response5 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post5));

			// agrego post6
			titulo = "Understanding React";
			texto = "Here is my second book. Learn how to code applications in React by following a solid path that starts by studying essential JavaScript constructions to then go into React core concepts to finally write an application. Understand how to split an application into components (class or function-based ones), how to.";
			tag = "React";
			links_relacionados = new String[] { "https://www.copypasteisforword.com/2021-12-25-understanding-react/",
					"https://leanpub.com/understandingreact" };
			autor = "Enrique Molinari";

			fecha = "2021-12-25T23:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post6 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response6 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post6));

			// agrego post7
			titulo = "Una arquitectura a prueba de balas para proyectos Node.js";
			texto = "Tabla de contenido La estructura de la carpeta 🏢 Arquitectura de 3 capas 🥪 Capa de servicio 💼 Capa de Pub/Sub 🎙️️ Inyección de dependencia 💉 Prueba unitaria 🕵🏻 Colas de trabajo y tarea recurrente ⚡ Configuraciones y secretos 🤫 Cargadores 🏗️ Ejemplo de repositorio";
			tag = "node";
			links_relacionados = new String[] { "https://softwareontheroad.com/es/ideal-nodejs-project-structure/" };
			autor = "Sam Quinn";

			fecha = "2019-04-17T10:10:36.408Z";
			// fecha = df2.parse(string2);

			Post post7 = new Post(titulo, texto, tag, links_relacionados, autor, fecha);

			IndexResponse response7 = esClient.index(i -> i.index("post")
					// .id(post1.getId())
					.document(post7));
		} catch (ElasticsearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
			 * catch (ParseException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

	}

}