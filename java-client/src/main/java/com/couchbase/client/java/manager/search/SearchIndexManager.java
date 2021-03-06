/*
 * Copyright (c) 2018 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.java.manager.search;

import com.couchbase.client.java.json.JsonObject;

import java.util.List;

import static com.couchbase.client.java.AsyncUtils.block;
import static com.couchbase.client.java.manager.search.AllowQueryingSearchIndexOptions.allowQueryingSearchIndexOptions;
import static com.couchbase.client.java.manager.search.AnalyzeDocumentOptions.analyzeDocumentOptions;
import static com.couchbase.client.java.manager.search.DisallowQueryingSearchIndexOptions.disallowQueryingSearchIndexOptions;
import static com.couchbase.client.java.manager.search.DropSearchIndexOptions.dropSearchIndexOptions;
import static com.couchbase.client.java.manager.search.FreezePlanSearchIndexOptions.freezePlanSearchIndexOptions;
import static com.couchbase.client.java.manager.search.GetAllSearchIndexesOptions.getAllSearchIndexesOptions;
import static com.couchbase.client.java.manager.search.GetIndexedSearchIndexOptions.getIndexedSearchIndexOptions;
import static com.couchbase.client.java.manager.search.GetSearchIndexOptions.getSearchIndexOptions;
import static com.couchbase.client.java.manager.search.PauseIngestSearchIndexOptions.pauseIngestSearchIndexOptions;
import static com.couchbase.client.java.manager.search.ResumeIngestSearchIndexOptions.resumeIngestSearchIndexOptions;
import static com.couchbase.client.java.manager.search.UnfreezePlanSearchIndexOptions.unfreezePlanSearchIndexOptions;
import static com.couchbase.client.java.manager.search.UpsertSearchIndexOptions.upsertSearchIndexOptions;

/**
 * The {@link SearchIndexManager} allows to manage search index structures in a couchbase cluster.
 *
 * @since 3.0.0
 */
public class SearchIndexManager {

  private final AsyncSearchIndexManager asyncIndexManager;

  public SearchIndexManager(final AsyncSearchIndexManager asyncIndexManager) {
    this.asyncIndexManager = asyncIndexManager;
  }

  /**
   * Fetches an index from the server if it exists.
   *
   * @param name the name of the search index.
   * @return the index definition if it exists.
   */
  public SearchIndex getIndex(final String name, final GetSearchIndexOptions options) {
    return block(asyncIndexManager.getIndex(name, options));
  }

  /**
   * Fetches all indexes from the server.
   *
   * @return all currently present indexes.
   */
  public List<SearchIndex> getAllIndexes(final GetAllSearchIndexesOptions options) {
    return block(asyncIndexManager.getAllIndexes(options));
  }

  /**
   * Retrieves the number of documents that have been indexed for an index.
   *
   * @param name the name of the search index.
   * @return the number of indexed documents.
   */
  public long getIndexedDocumentsCount(final String name, final GetIndexedSearchIndexOptions options) {
    return block(asyncIndexManager.getIndexedDocumentsCount(name, options));
  }

  /**
   * Allows to see how a document is analyzed against a specific index.
   *
   * @param name the name of the search index.
   * @param document the document to be analyzed.
   * @return the analyzed sections for the document.
   */
  public List<JsonObject> analyzeDocument(final String name, final JsonObject document, final AnalyzeDocumentOptions options) {
    return block(asyncIndexManager.analyzeDocument(name, document, options));
  }

  /**
   * Creates, or updates, an index.
   *
   * @param index the index definition including name and settings.
   */
  public void upsertIndex(final SearchIndex index, final UpsertSearchIndexOptions options) {
    block(asyncIndexManager.upsertIndex(index, options));
  }

  /**
   * Drops an index.
   *
   * @param name the name of the search index.
   */
  public void dropIndex(final String name, final DropSearchIndexOptions options) {
    block(asyncIndexManager.dropIndex(name, options));
  }

  /**
   * Pauses updates and maintenance for an index.
   *
   * @param name the name of the search index.
   */
  public void pauseIngest(final String name, final PauseIngestSearchIndexOptions options) {
    block(asyncIndexManager.pauseIngest(name, options));
  }

  /**
   * Resumes updates and maintenance for an index.
   *
   * @param name the name of the search index.
   */
  public void resumeIngest(final String name, ResumeIngestSearchIndexOptions options) {
    block(asyncIndexManager.resumeIngest(name, options));
  }

  /**
   * Allows querying against an index.
   *
   * @param name the name of the search index.
   */
  public void allowQuerying(final String name, AllowQueryingSearchIndexOptions options) {
    block(asyncIndexManager.allowQuerying(name, options));
  }

  /**
   * Disallows querying against an index.
   *
   * @param name the name of the search index.
   */
  public void disallowQuerying(final String name, final DisallowQueryingSearchIndexOptions options) {
    block(asyncIndexManager.disallowQuerying(name, options));
  }

  /**
   * Freeze the assignment of index partitions to nodes.
   *
   * @param name the name of the search index.
   */
  public void freezePlan(final String name, final FreezePlanSearchIndexOptions options) {
    block(asyncIndexManager.freezePlan(name, options));
  }

  /**
   * Unfreeze the assignment of index partitions to nodes.
   *
   * @param name the name of the search index.
   */
  public void unfreezePlan(final String name, final UnfreezePlanSearchIndexOptions options) {
    block(asyncIndexManager.unfreezePlan(name, options));
  }

  /**
   * Fetches an index from the server if it exists.
   *
   * @param name the name of the search index.
   * @return the index definition if it exists.
   */
  public SearchIndex getIndex(final String name) {
    return getIndex(name, getSearchIndexOptions());
  }

  /**
   * Fetches all indexes from the server.
   *
   * @return all currently present indexes.
   */
  public List<SearchIndex> getAllIndexes() {
    return getAllIndexes(getAllSearchIndexesOptions());
  }

  /**
   * Retrieves the number of documents that have been indexed for an index.
   *
   * @param name the name of the search index.
   * @return the number of indexed documents.
   */
  public long getIndexedDocumentsCount(final String name) {
    return getIndexedDocumentsCount(name, getIndexedSearchIndexOptions());
  }

  /**
   * Allows to see how a document is analyzed against a specific index.
   *
   * @param name the name of the search index.
   * @param document the document to be analyzed.
   * @return the analyzed sections for the document.
   */
  public List<JsonObject> analyzeDocument(final String name, final JsonObject document) {
    return analyzeDocument(name, document, analyzeDocumentOptions());
  }

  /**
   * Creates, or updates, an index.
   *
   * @param index the index definition including name and settings.
   */
  public void upsertIndex(final SearchIndex index) {
    upsertIndex(index, upsertSearchIndexOptions());
  }

  /**
   * Drops an index.
   *
   * @param name the name of the search index.
   */
  public void dropIndex(final String name) {
    dropIndex(name, dropSearchIndexOptions());
  }

  /**
   * Pauses updates and maintenance for an index.
   *
   * @param name the name of the search index.
   */
  public void pauseIngest(final String name) {
    pauseIngest(name, pauseIngestSearchIndexOptions());
  }

  /**
   * Resumes updates and maintenance for an index.
   *
   * @param name the name of the search index.
   */
  public void resumeIngest(final String name) {
    resumeIngest(name, resumeIngestSearchIndexOptions());
  }

  /**
   * Allows querying against an index.
   *
   * @param name the name of the search index.
   */
  public void allowQuerying(final String name) {
    allowQuerying(name, allowQueryingSearchIndexOptions());
  }

  /**
   * Disallows querying against an index.
   *
   * @param name the name of the search index.
   */
  public void disallowQuerying(final String name) {
    disallowQuerying(name, disallowQueryingSearchIndexOptions());
  }

  /**
   * Freeze the assignment of index partitions to nodes.
   *
   * @param name the name of the search index.
   */
  public void freezePlan(final String name) {
    freezePlan(name, freezePlanSearchIndexOptions());
  }

  /**
   * Unfreeze the assignment of index partitions to nodes.
   *
   * @param name the name of the search index.
   */
  public void unfreezePlan(final String name) {
    unfreezePlan(name, unfreezePlanSearchIndexOptions());
  }

}