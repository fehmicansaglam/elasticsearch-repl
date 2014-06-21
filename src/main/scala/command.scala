package net.fehmicansaglam.elasticsearch.repl

import org.parboiled2._

trait Command
case class Connect(host: String, port: Int) extends Command
case object Disconnect extends Command
case class Get(index: String, id: String)
case class Index(index: String, json: String)
case class Delete(index: String, id: String)
case class Search(index: String, query: Option[String])
case class Count(index: String, query: Option[String])
case class GetMappings(index: String)

class CommandParser(val input: ParserInput) extends Parser {

  def CommandLine = rule { WhiteSpace ~ Command ~ EOI }

  def Command = rule {
    DisconnectCommand | ConnectCommand | GetCommand | IndexCommand | DeleteCommand | SearchCommand | CountCommand | GetMappingsCommand
  }

  def ConnectCommand: Rule1[Connect] = rule {
    "connect" ~ (HostDefinition | (push("localhost") ~ push(9300))) ~> Connect
  }

  def DisconnectCommand: Rule1[Disconnect.type] = rule { "disconnect" ~ push(Disconnect) }

  def GetCommand: Rule1[Get] = rule {
    "get" ~ Id ~ "from" ~ IndexDefinition ~> ((id: String, index: String) => Get(index, id))
  }

  def IndexCommand: Rule1[Index] = rule {
    "index" ~ "into" ~ IndexDefinition ~ JsonDefinition ~> Index
  }

  def DeleteCommand: Rule1[Delete] = rule {
    "delete" ~ Id ~ "from" ~ IndexDefinition ~> ((id: String, index: String) => Delete(index, id))
  }

  def SearchCommand: Rule1[Search] = rule {
    "search" ~ "in" ~ IndexDefinition ~ optional(QueryDefinition) ~> Search
  }

  def CountCommand: Rule1[Count] = rule {
    "count" ~ IndexDefinition ~ optional(QueryDefinition) ~> Count
  }

  def GetMappingsCommand: Rule1[GetMappings] = rule {
    "mappings" ~ IndexDefinition ~> GetMappings
  }

  def HostDefinition: Rule2[String, Int] = rule { (Host ~ Port) | (Host ~ push(9300)) }

  def Host: Rule1[String] = rule { capture(oneOrMore(CharPredicate.AlphaNum)) ~ WhiteSpace }

  def Port: Rule1[Int] = rule {
    capture(oneOrMore(CharPredicate.Digit)) ~> ((port: String) => port.toInt) ~ WhiteSpace
  }

  def Id: Rule1[String] = rule { capture(oneOrMore(CharPredicate.Visible)) ~ WhiteSpace }

  def IndexDefinition: Rule1[String] = rule {
    capture(oneOrMore(CharPredicate.Visible)) ~ WhiteSpace
  }

  def JsonDefinition: Rule1[String] = rule {
    capture(oneOrMore(CharPredicate.Printable)) ~ WhiteSpace
  }

  def QueryDefinition: Rule1[String] = rule {
    capture(oneOrMore(CharPredicate.Visible)) ~ WhiteSpace
  }

  def WhiteSpace = rule { zeroOrMore(' ') }

  implicit def wspStr(s: String): Rule0 = rule {
    str(s) ~ WhiteSpace
  }

}