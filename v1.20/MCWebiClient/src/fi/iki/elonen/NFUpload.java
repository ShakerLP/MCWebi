package fi.iki.elonen;

import static fi.iki.elonen.NanoHTTPD.Method.POST;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;

/**
 * @author victor & ritchieGitHub
 */
public class NFUpload extends FileUpload {

	  public static class NanoHttpdContext implements UploadContext {

	        private NanoHTTPD.IHTTPSession session;

	        public NanoHttpdContext(NanoHTTPD.IHTTPSession session) {
	            this.session = session;
	        }

	        @Override
	        public long contentLength() {
	            long size;
	            try {
	                String cl1 = session.getHeaders().get("content-length");
	                size = Long.parseLong(cl1);
	            } catch (NumberFormatException var4) {
	                size = -1L;
	            }

	            return size;
	        }

	        @Override
	        public String getCharacterEncoding() {
	            return "UTF-8";
	        }

	        @Override
	        public String getContentType() {
	            return this.session.getHeaders().get("content-type");
	        }

	        @Override
	        public int getContentLength() {
	            return (int) contentLength();
	        }

	        @Override
	        public InputStream getInputStream() throws IOException {
	            return session.getInputStream();
	        }
	    }

	    public static final boolean isMultipartContent(NanoHTTPD.IHTTPSession session) {
	        return session.getMethod() == POST && FileUploadBase.isMultipartContent(new NanoHttpdContext(session));
	    }

	    public NFUpload(FileItemFactory fileItemFactory) {
	        super(fileItemFactory);
	    }

	    public List<FileItem> parseRequest(NanoHTTPD.IHTTPSession session) throws FileUploadException {
	        return this.parseRequest(new NanoHttpdContext(session));
	    }

	    public Map<String, List<FileItem>> parseParameterMap(NanoHTTPD.IHTTPSession session) throws FileUploadException {
	        return this.parseParameterMap(new NanoHttpdContext(session));
	    }

	    public FileItemIterator getItemIterator(NanoHTTPD.IHTTPSession session) throws FileUploadException, IOException {
	        return super.getItemIterator(new NanoHttpdContext(session));
	    }

}