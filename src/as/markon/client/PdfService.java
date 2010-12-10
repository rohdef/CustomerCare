package as.markon.client;

import java.util.List;

import as.markon.viewmodel.Company;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("pdf")
public interface PdfService extends RemoteService {
	public Integer createPdf(List<Company> companies);
}
