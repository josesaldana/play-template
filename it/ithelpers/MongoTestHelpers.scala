// Copyright (C) 2015 Jose Saldana.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ithelpers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import reactivemongo.api.{DB, MongoDriver}
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

import reactivemongo.api.collections.default._

trait MongoTestHelpers {

  def mongoServers() = { "localhost:27017" }
  def mongoDbName() = { "test" }

  def db:DB = {
    val driver = MongoDriver()
    val servers = mongoServers().split(",")
    val connection = driver.connection(servers.toSeq)

    connection.db(mongoDbName())
  }

  def newId(): String = BSONObjectID.generate.stringify

  def dbHelperFor(col: String): DBHelpers = new DBHelpers(db, col)

}

class DBHelpers(db: DB, col: String) {

  def collection = db.collection[BSONCollection](col)

  def create[T](dataFn: => T)(implicit writer: BSONDocumentWriter[T]): T = {
    val data = dataFn

    val p = () => {
      collection.insert(data).map { lastError =>
        if (lastError.inError) throw new Exception("Couldn't save the document")
        else data
      }
    }

    Await.result(p(), 10 seconds)
  }

  def create[T](dataFn: => Seq[T])(implicit writer: BSONDocumentWriter[T]): Seq[T] = {
    val data = dataFn

    val p = () => {
      collection.bulkInsert(Enumerator.enumerate(data)).map { n =>
        if (n != data.length) throw new Exception("Couldn't save the document")
        else data
      }
    }

    Await.result(p(), 10 seconds)
  }

  def saveJSON(json: JsValue*): Future[Seq[JsValue]] = ???

  def remove[T](q: (String, String)*)(implicit writer: BSONDocumentWriter[T]): Boolean = {
    val p = () => {
      val query = q map { field => BSONDocument(field._1 -> field._2) }

      collection.remove(query.head) map { lastError =>
        if(lastError.inError) throw new Exception("Couldn't delete the document")
        else true
      }
    }

    Await.result(p(), 10 seconds)
  }

  def find[T](q: => Map[String, String])(implicit reader: BSONDocumentReader[T]): List[T] = {
    val p = () => {
      val query = q map { field => BSONDocument(field._1 -> field._2) }
      collection.find(query.head).cursor[T].collect[List]()
    }

    Await.result(p(), 10 seconds)
  }

  def findOne[T](q: (String, String)*)(implicit reader: BSONDocumentReader[T]): Option[T] = {
    val p = () => {
      val query = q.foldLeft(BSONDocument.empty)((d,p) => d ++ BSONDocument(p._1 -> p._2))

      collection.find(query).cursor[T].headOption
    }

    Await.result(p(), 10 seconds)
  }

  def cleanCollection: Boolean = {
    val p = () => {
      val query = BSONDocument()

      collection.remove(query) map { lastError =>
        if(lastError.inError) throw new Exception("Couldn't clean the collection")
        else true
      }
    }

    Await.result(p(), 10 seconds)
  }

  def closeDB = db.connection.close()
}

