/**
 * Copyright 09/24/12 Raytheon Company.
 *
 * Unlimited Rights
 * This software was developed pursuant to Contract Number 
 * DTFAWA-10-D-00028 with the US Government. The US Government’s rights 
 * in and to this copyrighted software are as specified in DFARS
 * 252.227-7014 which was made part of the above contract. 
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.12.10 at 02:54:21 PM CST 
//

package com.raytheon.uf.edex.wfs.reg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.opengis.wfs.v_1_1_0.NativeType;

/**
 * <p>
 * Java class for unique complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="unique">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wfs}NativeType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "unique", propOrder = { "parameter" }, namespace = "http://edex.uf.raytheon.com")
public class Unique extends NativeType {

	@XmlElement(required = true)
	protected String parameter;

	/**
	 * Gets the value of the parameter property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * Sets the value of the parameter property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setParameter(String value) {
		this.parameter = value;
	}

}
