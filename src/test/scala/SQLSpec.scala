package com.lucidchart.open.relate.test

import com.lucidchart.open.relate._
import org.specs2.mutable._

class SQLSpec extends Specification {

  "The SQL method" should {
    
    "produce 0 parameters for a SQL statement with no parameters" in {
      val (query, params) = SqlStatementParser.parse("SELECT count(1) FROM table")
      params must have size(0)
    }

    "produce 1 parameter for a SQL statement with 1 parameter" in {
      val (query, params) = SqlStatementParser.parse("SELECT * FROM table WHERE param={name}")
      params must have size(1)
    }

    "produce 3 parameters for a SQL statement with 3 parameters" in {
      val (query, params) = SqlStatementParser.parse("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")
      params must have size(3)
    }

    "get the correct parameter names and in order" in {
      val (query, params) = SqlStatementParser.parse("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")
      val args = new Array[String](3)
      params.foreach { case (name, index) =>
        args(index(0) - 1) = name
      }
      args.toList must_== List("name1", "name2", "name3")
    }

    "have correct number of ?s in replaced query" in {
      val (query, params) = SqlStatementParser.parse("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")
      val numQuestionMarks = query.count(_ == '?')
      numQuestionMarks must_== 3
    }

    "strip out all original parameters" in {
      val (query, params) = SqlStatementParser.parse("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")
      val originalsRemoved = !((query contains '{') || (query contains '}'))
      originalsRemoved must beTrue
    }

    "handle repeated parameters" in {
      val (_, params) = SqlStatementParser.parse("INSERT INTO table (param1, param2) VALUES ({name1}, {name2}) ON DUPLICATE KEY UPDATE param1={name1}")

      params must have size(2)
      params("name1") must have size(2)
    }

    "handle curly braces escaped with {{ and }}" in {
      val (query, _) = SqlStatementParser.parse("""INSERT INTO table VALUES ("{{\"test\" : \"value\"}}")""")

      query must_== """INSERT INTO table VALUES ("{\"test\" : \"value\"}")"""
    }

    "handle a mix of escaped curly braces and parameters" in {
      val (query, _) = SqlStatementParser.parse("""INSERT INTO table VALUES ({test}, "{{\"test\" : \"value\"}}")""")

      query must_== """INSERT INTO table VALUES (?, "{\"test\" : \"value\"}")"""
    }
  }

}