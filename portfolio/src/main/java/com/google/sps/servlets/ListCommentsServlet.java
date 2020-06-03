// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.sps.data.Comment;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

import com.google.gson.Gson;

/** Servlet that returns some example content.*/
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    int nComments = getNCommentsChoice(request);

    if (nComments == -1) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter an integer between 1 and 3.");
      return;
    }
    
    List<Entity> topResults = results.asList(FetchOptions.Builder.withLimit(nComments));

    ArrayList<Comment> commentList = new ArrayList<Comment>();
    for (Entity entity : topResults) {
      String title = (String) entity.getProperty("title");
      String author = (String) entity.getProperty("author");
      Date timestamp = (Date) entity.getProperty("timestamp");
      String comment = (String) entity.getProperty("comment");
      long id = entity.getKey().getId();

      Comment commentObj = new Comment(title, author, timestamp, comment, id);
      commentList.add(commentObj);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(commentList));
  }

  /** Returns the number of comments entered by the user, or -1 if the choice was invalid. */
  private int getNCommentsChoice(HttpServletRequest request) {
    String nCommentsString = request.getParameter("nComments");
    int nComments;
    try {
      nComments = Integer.parseInt(nCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + nCommentsString);
      return -1;
    }

    if (nComments < 1) {
      System.err.println("Number of comments choice is out of range: " + nCommentsString);
      return -1;
    }

    return nComments;
  }
}