package com.azure.rest.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.ServiceUnavailableException;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Authentication {

	/*
	 * tenant_id can be found from your azure portal. Login into azure portal
	 * and browse to active directory and choose the directory you want to use.
	 * Then click on Applications tab and at the bottom you should see
	 * "View EndPoints". In the endpoints, the tenant_id will show up like this
	 * in the endpoint url's: https://login.microsoftonline.com/{tenant_id}
	 * https://blogs.msdn.microsoft.com/azureossds/2015/06/23/authenticating-azure-resource-management-rest-api-requests-using-java/
	 */
	private final static String AUTHORITY = "https://login.windows.net/{eff7f985-dc58-4935-a906-050609be85c3}";

	public static void main(String args[]) throws Exception {

		AuthenticationResult result = getAccessTokenFromUserCredentials();
		System.out.println("Access Token - " + result.getAccessToken());
		HttpClient client = new DefaultHttpClient();

		/*
		 * replace {subscription_id} with your subscription id and
		 * {resourcegroupname} with the resource group name for which you want
		 * to list the VM's.
		 */

		HttpGet request = new HttpGet(
				"https://management.azure.com/subscriptions/{64caccf3-b508-41e7-92ed-d7ed95b32621}/resourceGroups/{paw}/providers/Microsoft.ClassicCompute/virtualMachines?api-version=2014-06-01");
		request.addHeader("Authorization", "Bearer " + result.getAccessToken());
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
	}

	private static AuthenticationResult getAccessTokenFromUserCredentials()
			throws Exception {
		AuthenticationContext context = null;
		AuthenticationResult result = null;
		ExecutorService service = null;
		try {
			service = Executors.newFixedThreadPool(1);
			context = new AuthenticationContext(AUTHORITY, false, service);
			/*
			 * Replace {client_id} with ApplicationID and {password} with
			 * password that were used to create Service Principal above.
			 */
			ClientCredential credential = new ClientCredential(
					"{0f71f780-ee58-4b65-b466-bfea3e97fe50}",
					"{6nSRxitLcg/9z8TFKoOV/NJoJnGyVMt78g8+DlU6Dt8=}");
			Future<AuthenticationResult> future = context.acquireToken(
					"https://management.azure.com/", credential, null);
			result = future.get();
		} finally {
			service.shutdown();
		}
		if (result == null) {
			throw new ServiceUnavailableException(
					"authentication result was null");
		}
		return result;
	}

}
