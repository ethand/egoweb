package org.egonet.web;

import java.io.IOException;
import java.net.URL;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketServlet;

import org.egonet.web.page.IndexPage;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.mortbay.jetty.Server;

//import org.mortbay.jetty.handler.ResourceHandler;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import org.mortbay.resource.Resource;

public class Main extends WebApplication
{

	private static SessionFactory dbSessionFactory;

    public Main()
    {
		try {
			dbSessionFactory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
    		System.err.println("Caught an exception while trying to initialize hibernate session factory: "+ex);
		}
    }
    
    protected void onDestroy() {
    	dbSessionFactory.close();
    }
    
    public static SessionFactory getDBSessionFactory() {
    	return dbSessionFactory;
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
	@Override
	public Class<IndexPage> getHomePage() {
		return IndexPage.class;
	}


    // Mostly copied from http://www.codecommit.com/blog/java/so-long-wtp-embedded-jetty-for-me
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080); // TODO: Restrict access to localhost: 0.0.0.0 -> 192.168.1.1?
        
        /*
        Context staticContext = new Context(server,"/static",0);
        staticContext.setHandler(new ResourceHandler()); // maybe extend resource handler to pull from classpath
        staticContext.setResourceBase("./static/");
        staticContext.setContextPath("/static");
        */
        
        Context embeddedContext = new Context(server,"/static",0);
        ServletHolder embeddedHolder = new ServletHolder(new StaticContentFromClasspathServlet());
        embeddedContext.addServlet(embeddedHolder, "/*");
        
        
        Context context = new Context(server, "/", Context.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new WicketServlet());
        servletHolder.setInitParameter("applicationClassName",
                "org.egonet.web.Main"); 
        servletHolder.setInitOrder(1);
        context.addServlet(servletHolder, "/*");
 
        server.start();
        server.join();
    }
    
    public static class StaticContentFromClasspathServlet extends DefaultServlet {
    	public Resource getResource(String pathInContext) {
    		try {
    		URL url = getResourceFromClasspath(pathInContext);
    		return Resource.newResource(url);
    		} catch(IOException ex) {
    			System.err.println("Caught an IOException: "+ex);
    		}
    		return null;
    	}
    	public static URL getResourceFromClasspath(String name) {
    		if(name == null || name.length()==0)
    			return null;
    			
    		return StaticContentFromClasspathServlet.class.getResource(name);
    	}
    }
    
}

