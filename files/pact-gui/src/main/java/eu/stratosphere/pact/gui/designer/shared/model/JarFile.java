package eu.stratosphere.pact.gui.designer.shared.model;

import java.io.Serializable;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.client.event.PactProgramPropertyChangeEvent;

/**
 * Data model for jar files embedded into pact programs
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class JarFile implements Serializable {
	private static final long serialVersionUID = 6942672001817017639L;

	/**
	 * Empty constructor needed for serialization
	 */
	public JarFile() {
	}

	/*
	 * pact program wide unique ID of this object
	 */
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * reference to parent pact Program
	 */
	private PactProgram pactProgram = null;

	public void setPactProgram(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	/*
	 * FileName
	 */
	private String name = null;

	public void setName(String name) {
		setName(name, true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name, boolean fireEvent) {
		this.name = name;
		fireEvent(new PactProgramPropertyChangeEvent(pactProgram));
	}

	/*
	 * Hash of file content - needed of two files have the same name
	 */
	private String hash;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		setHash(hash, true);
	}

	public void setHash(String hash, boolean fireEvent) {
		this.hash = hash;
		fireEvent(new PactProgramPropertyChangeEvent(pactProgram));
	}

	/*
	 * binary jar filecontent
	 */
	private byte[] content = null;

	public void setContent(byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public void fireEvent(GwtEvent<?> event) {
		if (pactProgram != null) {
			if (pactProgram.getPactProgramManager() != null) {
				if (pactProgram.getPactProgramManager().getAppInjector() != null) {
					Log.debug("Fire event from JarFile-Model: " + event.toString());
					pactProgram.getPactProgramManager().getAppInjector().getEventBus().fireEvent(event);
				}
			}
		}
	}

	public String toString() {
		return "JarFile (hash:" + hash + "; name: " + name + ")";
	}
}
