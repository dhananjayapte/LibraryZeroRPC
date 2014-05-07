package edu.sjsu.cmpe.library.dto;

public class LinkDto {
    private String rel = "self"; // default is 'self'
    private String href = "#"; // default is '#'
    private String method = "GET"; // default is 'GET'

    /**
     * @param rel
     * @param href
     * @param method
     */
    public LinkDto(String rel, String href, String method) {
	super();
	this.rel = rel;
	this.href = href;
	this.method = method;
    }

    /**
     * @return the rel
     */
    public String getRel() {
	return rel;
    }

    /**
     * @param rel
     *            the rel to set
     */
    public void setRel(String rel) {
	this.rel = rel;
    }

    /**
     * @return the href
     */
    public String getHref() {
	return href;
    }

    /**
     * @param href
     *            the href to set
     */
    public void setHref(String href) {
	this.href = href;
    }

    /**
     * @return the method
     */
    public String getMethod() {
	return method;
    }

    /**
     * @param method
     *            the method to set
     */
    public void setMethod(String method) {
	this.method = method;
    }

}
