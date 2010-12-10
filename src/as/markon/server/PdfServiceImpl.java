package as.markon.server;

import java.util.List;

import org.apache.log4j.Logger;

import as.markon.client.PdfService;
import as.markon.viewmodel.Company;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class PdfServiceImpl extends RemoteServiceServlet implements PdfService {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(PdfServiceImpl.class);

	public Integer createPdf(List<Company> companies) {
		// TODO Add to DB
		return 1;
	}
}
