import java.io._
import scala.io.Source
import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.scm._
import cats.syntax.either._
import io.circe._ 
import io.circe.generic.auto._ 
import io.circe.parser._
import io.circe.syntax._
import io.circe.parser.decode


def eitherListToListJson(le:Either[io.circe.Error,List[Json]]):List[Json] = {
	val lj:List[Json] = {
		le match {
			case Right(l) => l
			case _ => throw new Exception("bad jason list")
		}
	}
	lj
}
def eitherListToListString(le:Either[io.circe.Error,List[String]]):List[String] = {
	val ls:List[String] = {
		le match {
			case Right(l) => l
			case _ => throw new Exception("bad jason list")
		}
	}
	ls
}


val lines = Source.fromFile("resources/PeriklesFinal.json").getLines.toList
val tempString:String = lines(0)
val jsString:String = tempString.replace("""'""",""""""")
val doc: Json = parse(jsString).getOrElse(Json.Null)

// We need a cursor to get stuff
val cursor: HCursor = doc.hcursor

// Get text catalog
val textNamesEither = cursor.downField("textnames").as[List[Json]]
val textNamesListJson:List[Json] = eitherListToListJson(textNamesEither)
val textNamesList:List[List[String]] = textNamesListJson.map(tn => {
	val textNameEither = tn.as[List[String]]
	eitherListToListString(textNameEither)
})
var tempCexCatalog:String = s"${cexHeader}\n\n${cexCatalogHeader}\n\n"
val tempCexCatalogEntries:String = textNamesList.map( tn => {
	 val versionComponentString:String = tn(1).replace(" ","")(0)
	 val versionUrn:CtsUrn = CtsUrn(s"${textUrnBase}:${versionComponentString}:")
	 val versionCitationScheme:String = citationScheme
	 val exemplarUrn:CtsUrn = CtsUrn(s"${versionUrn}.diffTokens:")
	 val exemplarCitationScheme:String = s"${versionCitationScheme}/token"
})

// Get text tokens
val allTextListEither = cursor.downField("alltexts").as[List[Json]]
val allTextListJson:List[Json] = eitherListToListJson(allTextListEither)
val allTextList:List[(List[(String)],Int)] = allTextListJson.map(atl => {
	val textJsonEither = atl.as[List[String]]
	eitherListToListString(textJsonEither)
}).zipWithIndex
// We want, for each token, the string, a pseudo-citation, and the index
val pseudoCts:List[(List[(String,String,Int)],Int)] = {
	allTextList.map( tt => {
		 val workNum:Int = tt._2
		 val thisTokenList:List[String] = tt._1
		 val thisTripleList:List[(String,String,Int)] = {
		 	thisTokenList.zipWithIndex.map( t => {
		 	 	val newEntry = {
		 	 		(t._1,s"${workNum}:${t._2}",t._2)
		 	 	} 
		 	 	newEntry
		 	})
		 }
		 (thisTripleList,workNum)
	})
}

def printPlutarchExemplars(pcts:List[(List[(String,String,Int)],Int)]):Unit = {
	val pw = new PrintWriter(new File("resources/cts-cex.txt" ))
	var levelOne:Int = 0
	var levelTwo:Int = 0
	var textContent:String = ""
	// Editions
	for (t <- pcts){
		levelOne = 0
		levelTwo = 0
		textContent = ""
		for (v <- t._1){
			val txt:String = v._1
			val wrk:String = v._2.split(":")(0)
			val tok:String = v._2.split(":")(1)
		   val enum:Int = v._3	
		   txt match {
		   	case t if t.matches("""[0-9]+\.""") => {
		   		if ( !(textContent.matches("""\s*"""))){
				   	pw.write(s"${wrk}:${levelOne}.${levelTwo}\t${textContent}\n")
		   		} 
			   	textContent = ""
			   	levelOne = levelOne + 1
			   	levelTwo = 0
		   	}
			   case t if t.matches("""\[[0-9]+\]""") => {
		   		if ( !(textContent.matches("""\s*"""))){
				   	pw.write(s"${wrk}:${levelOne}.${levelTwo}\t${textContent}\n")
				   }
			   	textContent = ""
		   		levelTwo = levelTwo + 1	
			   }
				case t if t.matches("""\s*""") =>
				case _ => {
					textContent = textContent + " " + txt
				}
		   } 
		} 
	}	
	// Analytical exemplars
	for (t <- pcts){
		levelOne = 0
		levelTwo = 0
		for (v <- t._1){
			val txt:String = v._1
			val wrk:String = v._2.split(":")(0)
			val tok:String = v._2.split(":")(1)
		   val enum:Int = v._3	
		   txt match {
		   	case t if t.matches("""[0-9]+\.""") => {
			   	levelOne = levelOne + 1
			   	levelTwo = 0
		   	}
			   case t if t.matches("""\[[0-9]+\]""") => {
		   		levelTwo = levelTwo + 1	
			   }
				case t if t.matches(""" *""") =>
				case _ => {
					pw.write(s"${wrk}.tokens:${levelOne}.${levelTwo}.${tok}\t${txt}\n")	
				}
		   } 
		} 
	}	
	pw.close
}

printPlutarchExemplars(pseudoCts)




//val textNameListEither = cursor.downField("textnames").as[List[Json]]
//val comparatioListEither = cursor.downField("comparatio").as[List[Json]]


/*
def jsonToCex(doc:List[Json]):String = {
	val cursor: HCursor = doc.hcursor
	val listEither = cursor.get[List[String]]
	val tokenList:List[String] = {
		listEither match {
			case Right(ls) => ls
			case _ => null
		}
	}
*/


// String Values

val textUrnBase:String = "urn:cts:greekLit:tlg0007.tlg012"
val citationScheme:String = "section/sentence"

val groupName:String = "Plutarch"
val workName:String = "Pericles"

val cexHeader:String = """
// A library demonstrating eComparatio data in CITE/CEX format
// Plutarch, Life of Perices, Greek.

#!cexversion
3.0

#!citelibrary
name#demo
urn#urn:cite2:cex:fufolio.2018_1:ecomparatioDemo
license#CC Share Alike.  For details, see more info.
"""

val cexCatalogHeader:String = """
#!ctscatalog
urn#citationScheme#groupName#workTitle#versionLabel#exemplarLabel#online#lang
"""


