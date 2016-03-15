/*
 * Copyright (c) 2011-2014, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.androidsdk.auth;

import android.test.InstrumentationTestCase;

import com.salesforce.androidsdk.TestCredentials;
import com.salesforce.androidsdk.auth.OAuth2.TokenEndpointResponse;
import com.salesforce.androidsdk.rest.RestResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Tests for HttpAccess.
 */
public class HttpAccessTest extends InstrumentationTestCase {

	private HttpAccess httpAccess;
	private OkHttpClient okHttpClient;
	private Headers headers;
	private HttpUrl resourcesUri;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		TestCredentials.init(getInstrumentation().getContext());
		httpAccess = new HttpAccess(null, "dummy-agent");
		okHttpClient = httpAccess.getOkHttpClient();
		TokenEndpointResponse refreshResponse = OAuth2.refreshAuthToken(httpAccess, new URI(TestCredentials.INSTANCE_URL), TestCredentials.CLIENT_ID, TestCredentials.REFRESH_TOKEN);
		Headers headers = new Headers.Builder()
				.add("Content-Type", "application/json")
				.add("Authorization", "OAuth " + refreshResponse.authToken)
				.build();
		HttpUrl resourcesUrl = HttpUrl.parse(TestCredentials.INSTANCE_URL + "/services/data/" + TestCredentials.API_VERSION + "/");
	}

	/**
	 * Testing sending a GET request to /services/data - Check status code and response body
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public void testDoGet() throws IOException, URISyntaxException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).get().build()).execute();
		checkResponse(response, HttpURLConnection.HTTP_OK, "sobjects", "identity", "recent", "search");
	}

	/**
	 * Testing sending a HEAD request to /services/data/vXX.X/ - Check status code and response body
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public void testDoHead() throws IOException, URISyntaxException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).head().build()).execute();
		assertEquals("200 response expected", HttpURLConnection.HTTP_OK, response.code());
	}
	
	/**
	 * Testing sending a POST request to /services/data/vXX.X/ - Check status code and response body
	 * @throws IOException 
	 */
	public void testSendPost() throws IOException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).post(null).build()).execute();
		checkResponse(response, HttpURLConnection.HTTP_BAD_METHOD, "'POST' not allowed");
	}

	/**
	 * Testing sending a PUT request to /services/data/vXX.X/ - Check status code and response body
	 * @throws IOException 
	 */
	public void testSendPut() throws IOException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).put(null).build()).execute();
		checkResponse(response,  HttpURLConnection.HTTP_BAD_METHOD, "'PUT' not allowed");
	}
	
	/**
	 * Testing sending a DELETE request to /services/data/vXX.X/ - Check status code and response body
	 * @throws IOException 
	 */
	public void testSendDelete() throws IOException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).delete().build()).execute();
		checkResponse(response,  HttpURLConnection.HTTP_BAD_METHOD, "'DELETE' not allowed");
	}

	/**
	 * Testing sending a PATCH request to /services/data/vXX.X/ - Check status code and response body
	 * @throws IOException 
	 */
	public void testSendPatch() throws IOException {
		Response response = okHttpClient.newCall(new Request.Builder().url(resourcesUri).headers(headers).patch(null).build()).execute();
		checkResponse(response,  HttpURLConnection.HTTP_BAD_METHOD, "'PATCH' not allowed");
	}
	
	/**
	 * Helper method to validate responses
	 * @param response
	 * @param expectedStatusCode
	 * @param stringsToMatch
	 */
	private void checkResponse(Response response, int expectedStatusCode, String... stringsToMatch) {
		// Check status code
		assertEquals(expectedStatusCode  + " response expected", expectedStatusCode, response.code());
		try {
			// Check body
			String responseAsString = new RestResponse(response).asString();
			for (String stringToMatch : stringsToMatch) {
				assertTrue("Response should contain " + stringToMatch, responseAsString.indexOf(stringToMatch) > 0);
			}
		} 
		catch (Exception e) {
			fail("Failed to read response body");
			e.printStackTrace();
		}
	}

	
}
