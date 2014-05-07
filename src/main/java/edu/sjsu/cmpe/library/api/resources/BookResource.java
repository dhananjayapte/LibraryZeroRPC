package edu.sjsu.cmpe.library.api.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.sjsu.cmpe.library.config.ConstantUtil;
import edu.sjsu.cmpe.library.domain.CommandWrapper;

import org.eclipse.jetty.server.HttpInput;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;
import org.zeromq.ZMQ;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.BookTest;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Message
public class BookResource {
    /** bookRepository instance */
    //private final BookRepositoryInterface bookRepository;
    ZMQ.Context context = null;
	ZMQ.Socket socket = null;

	public void connect(String address) {
		this.context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.REQ);
		// Socket to talk to server
		this.socket.connect(address);
	}

    /**
     * BookResource constructor
     * 
     * @param bookRepository
     *            a BookRepository instance
     */
    public BookResource(BookRepositoryInterface bookRepository) {
	//this.bookRepository = bookRepository;
	
	this.initializeHashMap();
    }
    
    public void initializeHashMap(){
    	//connect to server
    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
    	
    	//send to server and initialize HashMap
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	final MessagePack msgpack = new MessagePack();
    	Packer packer = msgpack.createPacker(out);
    	byte raw[] = null;
		try {
			raw = msgpack.write(new ConcurrentHashMap<Long, Book>());
			Value mapVal = msgpack.read(raw);
			Value values[] = new Value[1];
			values[0] = mapVal;
			
			//Book bookChck = msgpack.read(raw, Book.class);
			System.out.println("in book resource--- inside intialize map");
			
			packer.write(new CommandWrapper("createLibraryHashMap", values));
	        this.socket.send(out.toByteArray(), 0);
	        System.out.println("End of method: SendToServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @GET
    @Path("/{isbn}")
    @Timed(name = "view-book")
    public Response viewBookbyISBN(@PathParam("isbn") LongParam isbn, 
    		@Context HttpHeaders headers){
    	BookDto bookResponse = getBookByIsbn(isbn);
    	Book book = bookResponse.getBook();
    	System.out.println("Book is retrieved using getBookByIsbn--->"+book.getTitle());
    	
    	if(headers.getRequestHeader(headers.IF_MODIFIED_SINCE)!=null){
    		Date newDate = new Date(headers.getRequestHeader(headers.IF_MODIFIED_SINCE).get(0).toString());
    		ConcurrentHashMap<Long, Date> dateInMemoryMap = null; //bookRepository.getDateInMemoryMap();
    		//connect to server
        	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
        	
        	//send to server and initialize HashMap
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
        	final MessagePack msgpack = new MessagePack();
        	Packer packer = msgpack.createPacker(out);
    		try {
    			packer.write(new CommandWrapper("getDateInMemoryMap", null));
    	        this.socket.send(out.toByteArray(), 0);
    	        
    	        //listen ton server to get the Map
    	        dateInMemoryMap = getMapFromServer();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		if(dateInMemoryMap.contains(isbn.get())){
    			Date anotherDate = dateInMemoryMap.get(isbn.get());
    			if(newDate.compareTo(anotherDate)==0){
    				return Response.status(304).entity("Not Modified").build();
    			}
    		}else{
    			try {
        			byte[] rawIsbn = msgpack.write(isbn.get());
        			byte[] rawDate = msgpack.write(newDate);

        			Value values[] = new Value[2];
        			values[0] = msgpack.read(rawIsbn);
        			values[1] = msgpack.read(rawDate);
        			
        			//bookRepository.setDateInMemoryMap(isbn.get(), newDate);
        			packer.write(new CommandWrapper("setDateInMemoryMap", values));
        	        this.socket.send(out.toByteArray(), 0);
        	        
        		}catch(Exception e){
        			e.printStackTrace();
        		}
    		}
    	}
    	
    	AuthorDto authorLinks = new AuthorDto();
		if(book.getAuthorList()!=null && !book.getAuthorList().isEmpty()){
			for(Author authorObj : book.getAuthorList()){
				authorLinks.addLink(new LinkDto("view-author", "/books/" + 
						book.getIsbn() + "/authors/" + authorObj.getId(), "GET"));
			}
		}
		
		ReviewDto reviewLinks = new ReviewDto();
		if(book.getReviewList()!=null && book.getReviewList().size() > 0){
			for(Review reviewObj : book.getReviewList()){
				reviewLinks.addLink(new LinkDto("view-review", "/books/" + book.getIsbn() + 
						"/reviews/" + reviewObj.getId(), "GET"));
			}
		}
		
		Map<String, Object> bookMap = new HashMap<String, Object>();
		bookMap.put("isbn", book.getIsbn());
		bookMap.put("title", book.getTitle());
		bookMap.put("publication-date", book.getPublication_date());
		bookMap.put("language", book.getLanguage());
		bookMap.put("num-pages", book.getNum_pages());
		bookMap.put("status", book.getStatus());
		bookMap.put("reviews", reviewLinks.getLinks());
		bookMap.put("authors", authorLinks.getLinks());
		
		
		Map<String, Object> displayMap = new HashMap<String, Object>();
		displayMap.put("book", bookMap);
		displayMap.put("links", bookResponse.getLinks());
		
		return Response.status(200).entity(displayMap).build();
    }
    
    private ConcurrentHashMap<Long, Date> getMapFromServer() throws IOException {
		//listen to ZeroRPC server to get the reply from server
		MessagePack packer = new MessagePack();
		byte[] response = this.socket.recv(0);
		ConcurrentHashMap<Long, Date> dateInMemoryMap =  packer.createUnpacker(new ByteArrayInputStream(response)).read(new ConcurrentHashMap<Long, Date> ());
		return dateInMemoryMap;
	}
    
   
    public BookDto getBookByIsbn(@PathParam("isbn") LongParam isbn) {
		//Book book = bookRepository.getBookByISBN(isbn.get());
    	Book book = null;
    	
		//connect to server
    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
    	//retrieve the isbn value
    	Long isbnValue = isbn.get();
    	
    	final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final MessagePack msgpack = new MessagePack();
		Packer packer = msgpack.createPacker(out);
		
		//send the value to ZRPC server
		byte raw[] = null;
		try {
			raw = msgpack.write(isbnValue);
			Value val = msgpack.read(raw);
			Value values[] = new Value[1];
			values[0] = val;
			
			//System.out.println("in library check b4 send::"+bookChck.getTitle());
			
			packer.write(new CommandWrapper("getBookByIsbn", values));
	        this.socket.send(out.toByteArray(), 0);
	        System.out.println("End of method: SendToServer");
	        
	        //listen to ZRPC Server and get the value
			book = listenToServer();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		BookDto bookResponse = new BookDto(book);
		bookResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(), "GET"));
		bookResponse.addLink(new LinkDto("update-book", "/books/" + book.getIsbn(), "PUT"));
		
		// add more links
		bookResponse.addLink(new LinkDto("delete-book", "/books/" + book.getIsbn(), "DELETE"));
		bookResponse.addLink(new LinkDto("create-review", "/books/" + book.getIsbn() + "/reviews", "POST"));
	
		return bookResponse;
    }

    @POST
    @Timed(name = "create-book")
    public Response createBook(@Valid Book request) throws IOException {
    	List<String> statusList = Arrays.asList("available", "check-out", "in-queue","lost");
    	String status = request.getStatus();
    	if(status == null || status.isEmpty()){
    		request.setStatus("available");;
    	}else if(!statusList.contains((status.toLowerCase()))){
    		return Response.status(422).type("text/plain")
    				.entity("Wrong status value. Status should be one of the follwoing: available, check-out, in-queue or lost")
    				.build();
    	}else{
    		request.setStatus(status.toLowerCase());
    	}
    	
    	if(request.getAuthorList() == null || request.getAuthorList().isEmpty()){
    		return Response.status(422).type("text/plain").entity("Author cannot be empty").build();
    	}else{
    		for(Author authorObj : request.getAuthorList()){
    			if(authorObj.getName()==null || authorObj.getName().length()==0){
    				return Response.status(422).type("text/plain").entity("Author Name cannot be empty").build();
    			}
    		}
    	}
    	
    	System.out.println("Inside create book!!");
    	//connect to server
    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
    	//pack the book object using msg packer
    	// Store the new book in the BookRepository so that we can retrieve it.
    	this.sendToServer("saveBook", request);
    	
		//Book savedBook = bookRepository.saveBook(request);
	
		Book savedBook = listenToServer();
		
		BookDto bookResponse = null;
		//extract the links to create a response and send back the response
		if(savedBook!=null){
			String location = "/books/" + savedBook.getIsbn();
			bookResponse = new BookDto(savedBook);
			bookResponse.addLink(new LinkDto("view-book", location, "GET"));
			bookResponse.addLink(new LinkDto("update-book", location, "PUT"));
			
			// Add other links if needed
			bookResponse.addLink(new LinkDto("delete-book", location, "DELETE"));
			bookResponse.addLink(new LinkDto("create-review", location + "/reviews", "POST"));
			if(savedBook.getReviewList()!=null && !savedBook.getReviewList().isEmpty()){
				bookResponse.addLink(new LinkDto("view-all-reviews", "/books/" + savedBook.getIsbn() + "/reviews", "GET"));
			}
			
			/*Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("links", bookResponse.getLinks());*/
		}
		return Response.status(201).entity(createDisplayMap(bookResponse)).build();
    }

	/**
	 * @return
	 * @throws IOException
	 */
	private Book listenToServer() throws IOException {
		//listen to ZeroRPC server to get the reply from server
		MessagePack packer = new MessagePack();
		byte[] response = this.socket.recv(0);
		Book savedBook =  packer.createUnpacker(new ByteArrayInputStream(response)).read(new Book());
		return savedBook;
	}
	
    public void sendToServer(String methodName, Book bookTestObj){
    	System.out.println("Send to ZeroRPC Server");
    	final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final MessagePack msgpack = new MessagePack();
		Packer packer = msgpack.createPacker(out);

		byte raw[] = null;
		try {
			raw = msgpack.write(bookTestObj);
			Value mapVal = msgpack.read(raw);
			Value values[] = new Value[1];
			values[0] = mapVal;
			
			Book bookChck = msgpack.read(raw, Book.class);
			System.out.println("in library check b4 send::"+bookChck.getTitle());
			
			packer.write(new CommandWrapper(methodName, values));
	        this.socket.send(out.toByteArray(), 0);
	        System.out.println("End of method: SendToServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @DELETE
    @Path("/{isbn}")
    @Timed(name = "delete-book")
    public Response deleteBook(@PathParam("isbn") LongParam isbn){
    	//bookRepository.deleteBook(isbn.get());
    	performActionByIsbn(isbn.get(), "deleteBook");
    	BookDto bookResponse = new BookDto();
		bookResponse.addLink(new LinkDto("create-book", "/books", "POST"));
	
    	return Response.status(200).entity(createDisplayMap(bookResponse)).build();
    }
    /**
     * This method will perform the specified action i.e. it will send 
     * a Request to ZRPC server to execute the specified method
     * @param isbn
     * @param methodName
     */
    private void performActionByIsbn(Long isbn, String methodName){
		//connect to server
    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
    	//retrieve the isbn value
    	Long isbnValue = isbn;
    	
    	final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final MessagePack msgpack = new MessagePack();
		Packer packer = msgpack.createPacker(out);
		
		//send the value to ZRPC server
		byte raw[] = null;
		try {
			raw = msgpack.write(isbnValue);
			Value val = msgpack.read(raw);
			Value values[] = new Value[1];
			values[0] = val;
			
			//System.out.println("in library check b4 send::"+bookChck.getTitle());
			
			packer.write(new CommandWrapper(methodName, values));
	        this.socket.send(out.toByteArray(), 0);
	        System.out.println("End of method perform "+ methodName);
	        
	        //listen to ZRPC Server and get the value
			//book = listenToServer();
			
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
    /**
     * This method will create a Map o display the desired links to the user
     * @param bookResponse
     * @return
     */
    private Map<String, Object> createDisplayMap(BookDto bookResponse){
    	Map<String, Object> displayMap = new HashMap<String, Object>();
		displayMap.put("links", bookResponse.getLinks());
		return displayMap;
    }
    
    @PUT
    @Path("/{isbn}")
    @Timed(name = "update-book")
    public Response updateBookByIsbn(@PathParam("isbn") LongParam isbn, @QueryParam("status") String status){
    	List<String> statusList = Arrays.asList("available", "check-out", "in-queue","lost");
    	if(status == null || status.isEmpty()){
    		status = "available";
    	}else if(!statusList.contains(status.toLowerCase())){
    		return Response.status(422).type("text/plain").entity("Wrong status value").build();
    	}else{
    		status.toLowerCase();
    	}
    	
    	System.out.println("Send to ZeroRPC Server");
    	final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final MessagePack msgpack = new MessagePack();
		Packer packer = msgpack.createPacker(out);

		Book updatedBook = null;
		byte rawIsbn[] = null;
		byte rawStatus[] = null;
		try {
			rawIsbn = msgpack.write(isbn.get());
			rawStatus = msgpack.write(status);
			Value isbnVal = msgpack.read(rawIsbn);
			Value statusVal = msgpack.read(rawStatus);
			Value values[] = new Value[2];
			values[0] = isbnVal;
			values[1] = statusVal;
			
			//send request to ZRPC server
			//connect to server
	    	this.connect("tcp://"+ ConstantUtil.SERVER_ADDRESS +":4242");
	    	//pack the book object using msg packer
	    	packer.write(new CommandWrapper("updateBookByIsbn", values));
	        this.socket.send(out.toByteArray(), 0);
	        System.out.println("updateBookByIsn Sent to Server");
			
			//Listen to server
			updatedBook = listenToServer();
		}catch(Exception e){
			e.printStackTrace();
		}
    	
    	//Book updatedBook = bookRepository.updateBook(isbn.get(),status);
		
    	//update the links 
		BookDto bookResponse = new BookDto(updatedBook);
		bookResponse.addLink(new LinkDto("view-book", "/books/" + updatedBook.getIsbn(), "GET"));
		bookResponse.addLink(new LinkDto("update-book", "/books/" + updatedBook.getIsbn(), "PUT"));
		bookResponse.addLink(new LinkDto("delete-book", "/books/" + updatedBook.getIsbn(), "DELETE"));
		bookResponse.addLink(new LinkDto("create-review", "/books/" + updatedBook.getIsbn(), "POST"));
		if(updatedBook.getReviewList()!=null && !updatedBook.getReviewList().isEmpty()){
			bookResponse.addLink(new LinkDto("view-all-reviews", "/books/" + updatedBook.getIsbn(), "GET"));
		}
		
		return Response.status(200).entity(createDisplayMap(bookResponse)).build();
    }
    
	/**
	 * This method will send a HashMap to the server and receive a updated HashMap from the server
	 * @param methodName
	 * @param userMap
	 * @throws IOException
	 */
	public void sendHashMap(String methodName, Map<Object, Object> userMap) throws IOException{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final MessagePack msgpack = new MessagePack();
		Packer packer = msgpack.createPacker(out);

		//send to server
		byte raw[] = msgpack.write(userMap);
		Value mapVal = msgpack.read(raw);
		Value values[] = new Value[1];
		values[0] = mapVal;
		packer.write(new CommandWrapper(methodName, values));
        this.socket.send(out.toByteArray(), 0);
		
		//receive from server
		byte[] response = this.socket.recv(0);
		ByteArrayInputStream in = new ByteArrayInputStream(response);
        Unpacker unpacker = msgpack.createUnpacker(in);
        System.out.println("Incoming class is -->>" +unpacker.getNextType());
        if(unpacker.getNextType() == ValueType.RAW){
        	System.out.println("Exception -->> "+ unpacker.readString());
        }else{
        	System.out.println("Result Received from Server is-->");
        	 Map<String, String> resultMap =  msgpack.read(raw, Templates.tMap(Templates.TString, Templates.TString));
        	 if(resultMap!=null && !resultMap.isEmpty()){
        		 for(int i=0; i<resultMap.size();i++){
        			 System.out.println("Key-->"+i+" Value-->" + resultMap.get(""+i));
        		 }
        	 }
        }
	}
    
}
