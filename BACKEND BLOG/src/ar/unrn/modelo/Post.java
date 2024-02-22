package ar.unrn.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {

	private Integer id;
	private String titulo;
	private String texto;
	private String tag;
	private String[] links_relacionados;
	private String autor;
	private String fecha;
	
	@JsonCreator
	public Post(@JsonProperty("titulo") String titulo, 
			@JsonProperty("texto") String texto, 
			@JsonProperty("tag") String tag, 
			@JsonProperty("links_relacionados") String[] links_relacionados,
			@JsonProperty("autor") String autor, 
			@JsonProperty("fecha") String fecha) {
		this.titulo = titulo;
		this.texto = texto;
		this.tag = tag;
		this.links_relacionados = links_relacionados;
		this.autor = autor;
		this.fecha = fecha;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public  String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String[] getLinks_relacionados() {
		return links_relacionados;
	}

	public void setLinks_relacionados(String[] links_relacionados) {
		this.links_relacionados = links_relacionados;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	
}
