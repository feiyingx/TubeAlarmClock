package com.tubealarmclock.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.tubealarmclock.code.Constants;


public class YouTubeSearchService {
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
	private static YouTube youtube;
	
	public static List<SearchResult> Search(String queryText){
		try {
		
			if(youtube == null){
				youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest arg0) throws IOException {}
				}).setApplicationName("com.tubealarmclock.data").build();
			}
			
			YouTube.Search.List search;
			
			search = youtube.search().list("id,snippet");
			search.setKey(Constants.YOUTUBE_API_KEY);
			search.setQ(queryText);
			search.setType("video");
			search.setFields("items(id/kind,id/videoId,snippet/title)");
			search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
			SearchListResponse response = search.execute();
			
			List<SearchResult> results = response.getItems();
			return results;		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
