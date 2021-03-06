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

package helpers

import models.Persistable
import models.datastore.DataStore

import play.api.libs.json.{ Writes, Reads }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

import org.specs2.mock._

trait MockDataStore extends DataStore with Mockito {

  var mock: DataStore = mock[DataStore]

  def find[T](id: String)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Option[T]] = mock.find[T](id)

  def find[T](fields: (String, String)*)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Seq[T]] = ???

  def findAll[T](implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Seq[T]] = {
    mock.findAll[T]
  }

  def persist[T](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    writer: Writes[T],
    ec: ExecutionContext): Future[Boolean] = mock.persist[T](obj)

  def modify[T <: Persistable](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    writer: Writes[T],
    ec: ExecutionContext): Future[Boolean] = mock.modify[T](obj)

  def remove[T <: Persistable](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Boolean] = mock.remove[T](obj)
}
