package com.twitter.finagle.exp.zookeeper

import com.twitter.io.Buf


case class Packet(header: Option[RequestHeader], request: Option[Request]) {

  def buf: Buf = {

    header match {
      case Some(requestHeader: RequestHeader) =>
        request match {
          case Some(req: Request) =>
            requestHeader.buf
              .concat(req.buf)
          case None => requestHeader.buf
        }
      case None =>
        request match {
          case Some(req: ConnectRequest) => req.buf
        }
    }
  }
}

case class RequestRecord(opCode: Int, xid: Option[Int])