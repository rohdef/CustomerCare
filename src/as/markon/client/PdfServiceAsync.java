package as.markon.client;

import java.util.List;

import as.markon.viewmodel.Company;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PdfServiceAsync {

	void createPdf(List<Company> companies, AsyncCallback<Integer> callback);

}
