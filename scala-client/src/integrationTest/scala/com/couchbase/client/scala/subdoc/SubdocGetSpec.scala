package com.couchbase.client.scala.subdoc

import com.couchbase.client.core.error.subdoc.PathNotFoundException
import com.couchbase.client.core.error.{DecodingFailedException, DocumentDoesNotExistException, TemporaryLockFailureException}
import com.couchbase.client.scala.kv.LookupInSpec
import com.couchbase.client.scala.{Cluster, TestUtils}
import org.scalatest.FunSuite

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import com.couchbase.client.scala.kv.LookupInSpec._

class SubdocGetSpec extends FunSuite {
  val (cluster, bucket, coll) = (for {
    cluster <- Cluster.connect("localhost", "Administrator", "password")
    bucket <- cluster.bucket("default")
    coll <- bucket.defaultCollection()
  } yield (cluster, bucket, coll)) match {
    case Success(result) => result
    case Failure(err) => throw err
  }

  test("no commands") {
    val docId = TestUtils.docId()
    coll.lookupIn(docId, Array[LookupInSpec]()) match {
      case Success(result) => assert(false, s"unexpected success")
      case Failure(err: IllegalArgumentException) =>
      case Failure(err) =>
        assert(false, s"unexpected error $err")
    }
  }


  test("lookupIn") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> "world",
      "foo" -> "bar",
      "age" -> 22)
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId, Array(get("foo"),get("age"))) match {
      case Success(result) =>
        assert(result.cas != 0)
        assert(result.cas == insertResult.cas)
        assert(result.contentAs[String](0).get == "bar")
        assert(result.contentAs[Int](1).get == 22)
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }


  test("path does not exist single") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> "world")
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId, Array(get("not_exist"))) match {
      case Success(result) => assert(false, s"should not succeed")
      case Failure(err: PathNotFoundException) =>
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }


  test("path does not exist multi") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> "world")
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId, Array(get("not_exist"), get("hello"))) match {
      case Success(result) =>
        result.contentAs[String](0) match {
          case Success(body) => assert(false, s"should not succeed")
          case Failure(err: PathNotFoundException) =>
          case Failure(err) => assert(false, s"unexpected error $err")
        }
        assert(result.contentAs[String](1).get == "world")
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }

  test("lookupIn with doc") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> "world",
      "foo" -> "bar",
      "age" -> 22)
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId, Array(get("foo"), get("age"), getDoc)) match {
      case Success(result) =>
        assert(result.contentAs[String](0).get == "bar")
        result.contentAs[ujson.Obj](2) match {
          case Success(body) =>
            assert(body("hello").str == "world")
            assert(body("age").num == 22)
          case Failure(err) => assert(false, s"unexpected error $err")
        }
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }

  test("exists single") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> ujson.Arr("world"))
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId,
      Array(exists("does_not_exist"))) match {
      case Success(result) =>
        result.contentAs[Boolean](0) match {
          case Failure(err: PathNotFoundException) =>
          case Success(v) => assert(false, s"should not succeed")
          case Failure(err) => assert(false, s"unexpected error $err")
        }
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }

  test("exists multi") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> ujson.Arr("world"),
      "foo" -> "bar",
      "age" -> 22)
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId,
      Array(count("hello"), exists("age"), exists("does_not_exist"))) match {
      case Success(result) =>
        assert(result.exists(0))
        assert(result.exists(1))
        assert(!result.exists(2))
        assert(result.contentAs[Boolean](1).get)
        result.contentAs[Boolean](2) match {
          case Failure(err: PathNotFoundException) =>
          case Success(v) => assert(false, s"should not succeed")
          case Failure(err) => assert(false, s"unexpected error $err")
        }
        result.contentAs[String](1) match {
          case Failure(err: DecodingFailedException) =>
          case Success(v) => assert(false, s"should not succeed")
          case Failure(err) => assert(false, s"unexpected error $err")
        }
        assert(result.contentAs[Int](0).get == 1)
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }

  test("count") {
    val docId = TestUtils.docId()
    coll.remove(docId)
    val content = ujson.Obj("hello" -> ujson.Arr("world"),
      "foo" -> "bar",
      "age" -> 22)
    val insertResult = coll.insert(docId, content).get

    coll.lookupIn(docId,
      Array(count("hello"), exists("age"), exists("does_not_exist"))) match {
      case Success(result) =>
        assert(result.contentAs[Boolean](1).get)
        result.contentAs[Boolean](2) match {
          case Failure(err: PathNotFoundException) =>
          case Success(v) => assert(false, s"should not succeed")
          case Failure(err) => assert(false, s"unexpected error $err")
        }
        result.contentAs[String](1) match {
          case Failure(err: DecodingFailedException) =>
          case Success(v) => assert(false, s"should not succeed")
          case Failure(err) => assert(false, s"unexpected error $err")
        }
        assert(result.contentAs[Int](0).get == 1)
      case Failure(err) => assert(false, s"unexpected error $err")
    }
  }
}