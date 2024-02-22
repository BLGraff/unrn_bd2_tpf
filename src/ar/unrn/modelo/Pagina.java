package ar.unrn.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pagina {

	private String id;
	private String titulo;
	private String texto;
	private String autor;
	private String fecha;
	
	@JsonCreator
	public Pagina(@JsonProperty("titulo") String titulo, 
			@JsonProperty("texto") String texto, 
			@JsonProperty("autor") String autor, 
			@JsonProperty("fecha") String fecha) {
		this.titulo = titulo;
		this.texto = texto;
		this.autor = autor;
		this.fecha = fecha;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
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
