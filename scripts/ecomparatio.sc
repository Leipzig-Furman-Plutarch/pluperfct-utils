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


/* 

Eventually Parameterize These Values!
(Data that is not in the JSON, but probably should be.)

*/

val textUrnBase:String = "urn:cts:greekLit:tlg0007.tlg012"
val citationScheme:String = "section/sentence"

val groupName:String = "Plutarch"
val workTitle:String = "Pericles"
val textLang:String = "grc"
val diffExemplarUrnComponent:String = "diffTokens"
val primarySeparator:String = "#"

val cexFilePath:String = "resources/cts-cex.txt"

val cexHeader:String = """
// A library demonstrating eComparatio data in CITE/CEX format
// Plutarch, Life of Perices, Greek.

#!cexversion
3.0

#!citelibrary
name#demo
urn#urn:cite2:cex:fufolio.2018_1:ecomparatioDemo
license#CC Share Alike.  For details, see <https://github.com/Leipzig-Furman-Plutarch/pluperfct-utils>.
"""

val cexCatalogHeader:String = """
#!ctscatalog
urn#citationScheme#groupName#workTitle#versionLabel#exemplarLabel#online#lang
"""


/*

Utility Methods for dealing with JSON

*/

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

def textNameStringToVersionUrn(tn:List[String]):CtsUrn = {
	 val versionComponentString:String = tn(1).replace(" ","")
	 val versionUrn:CtsUrn = CtsUrn(s"${textUrnBase}.${versionComponentString}:")
	 versionUrn
}

def textNameStringToExemplarUrn(tn:List[String]):CtsUrn = {
	 val versionComponentString:String = tn(1).replace(" ","")
	 val exemplarUrn:CtsUrn = CtsUrn(s"${textUrnBase}.${versionComponentString}.${diffExemplarUrnComponent}:")	 
	 exemplarUrn
}


/* Process JSON file into CEX */

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
var tempCexCatalog:String = s"${cexHeader}\n\n${cexCatalogHeader}"

// Get Vector of Map["version"->CtsUrn,"exemplar"->CtsUrn]
val corpusUrns:Vector[Map[String,CtsUrn]] = {
	textNamesList.map( tn =>{
	  	val vUrn = textNameStringToVersionUrn(tn)
	  	val eUrn = textNameStringToExemplarUrn(tn)
	  	val urnMap:Map[String,CtsUrn] = Map("version" -> vUrn, "exemplar" -> eUrn)
	  	urnMap
	} ).toVector
}

val tempCexCatalogEntries:String = textNamesList.map( tn => {
	 val versionComponentString:String = tn(1).replace(" ","")
	 val versionUrn:CtsUrn = CtsUrn(s"${textUrnBase}.${versionComponentString}:")
	 val versionCitationScheme:String = citationScheme
	 val exemplarUrn:CtsUrn = CtsUrn(s"${textUrnBase}.${versionComponentString}.${diffExemplarUrnComponent}:")	 
	 val exemplarCitationScheme:String = s"${versionCitationScheme}/token"
	 val versionLabel:String = s"${tn(1)} ${tn(2)} ${tn(3)}"
	 val exemplarLabel:String = s"tokenized for comparison"
	 var tempString:String = versionUrn.toString + '#'
	 tempString = tempString + versionCitationScheme + '#'
	 tempString = tempString +  groupName + '#' + workTitle + '#' + versionLabel + "##true#" + textLang + "\n"
	 tempString = tempString +  exemplarUrn.toString + '#'
	 tempString = tempString +  exemplarCitationScheme + '#'
	 tempString = tempString +  groupName + '#' + workTitle + '#' + versionLabel + '#' 
	 tempString = tempString +  exemplarLabel + "#true#" + textLang 
	 tempString
}).mkString("\n")
tempCexCatalog = tempCexCatalog + tempCexCatalogEntries
val cexCatalog:String = tempCexCatalog

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

/* 

Notes for the Future:

	- This blindly captures a citation scheme specific to this JSON
	- Sections look like "1.", subsections look like "[1]"

*/
def ctsTextFromJson(pcts:List[(List[(String,String,Int)],Int)]):String = {
	val sb = new StringBuilder()
	sb.append(cexCatalog)
	sb.append("\n\n")
	sb.append("#!ctsdata\n")
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
			//val wrk:String = v._2.split(":")(0)
			val wrk:String = corpusUrns(v._2.split(":")(0).toInt)("version").toString
			val tok:String = v._2.split(":")(1)
		   val enum:Int = v._3	
		   txt match {
		   	case t if t.matches("""[0-9]+\.""") => {
		   		if ( !(textContent.matches("""\s*"""))){
				   	sb.append(s"${wrk}${levelOne}.${levelTwo}${primarySeparator}${textContent}\n")
		   		} 
			   	textContent = ""
			   	levelOne = levelOne + 1
			   	levelTwo = 0
		   	}
			   case t if t.matches("""\[[0-9]+\]""") => {
		   		if ( !(textContent.matches("""\s*"""))){
				   	sb.append(s"${wrk}${levelOne}.${levelTwo}${primarySeparator}${textContent}\n")
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
			val wrk:String = corpusUrns(v._2.split(":")(0).toInt)("exemplar").toString
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
					sb.append(s"${wrk}${levelOne}.${levelTwo}.${tok}${primarySeparator}${txt}\n")	
				}
		   } 
		} 
	}	
	val cexString:String = sb.toString
	cexString
}

val eComparatioCEX = ctsTextFromJson(pseudoCts)

def writeCEX(cex:String):Unit = {
	val pw = new PrintWriter(new File(cexFilePath))
	pw.write(cex)
	pw.close
}

// Write CEX File
writeCEX(eComparatioCEX)

// Test it by loading cexFilePath into a Cite Library
val cexData = Source.fromFile("resources/cts-cex.txt").getLines.mkString("\n")
val library = CiteLibrary(cexData,"#",",")



