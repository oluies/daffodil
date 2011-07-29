package daffodil.xml

import java.io.FileInputStream
import java.io.File
import java.io.InputStream
import org.jdom.input.SAXBuilder
/**
 * Copyright (c) 2010 NCSA.  All rights reserved.
 * Developed by: NCSA Cyberenvironments and Technologies
 *               University of Illinois at Urbana-Champaign
 *               http://cet.ncsa.uiuc.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the names of NCSA, University of Illinois, nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 *
 */

/*
 * Created By: Alejandro Rodriguez < alejandr @ ncsa . uiuc . edu >
 * Date: 2010
 */

import scala.collection.JavaConversions.asBuffer
import scala.xml.Node

import java.io.{OutputStream, PrintWriter, StringWriter}
import java.lang.management._
import java.util.regex.Pattern

import org.jdom.Attribute
import org.jdom.Element
import org.jdom.Parent
import org.jdom.Document
import org.jdom.Namespace
import org.jdom.output.XMLOutputter
import org.jdom.output.Format

import daffodil.processors.VariableMap
import daffodil.processors.xpath.NodeResult
import daffodil.processors.xpath.StringResult
import daffodil.processors.xpath.XPathUtil
import daffodil.exceptions.DFDLSchemaDefinitionException
import daffodil.schema.annotation._
import daffodil.debugger.DebugUtil
import daffodil.parser.{AnnotationParser, LinkedList}
import daffodil.parser.regex.Regex


/**
 * Utilities for handling XML 
 *
 * @version 1
 * @author Alejandro Rodriguez
 */
object XMLUtil {

  val MAX_MEMORY_PERCENTAGE = 0.90
  var WARNING_MEMORY_PERCENTAGE = 0.8
  var nodeCount:Long = 0


  val XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema"
  val DFDL_NAMESPACE = "http://www.ogf.org/dfdl/dfdl-1.0/"
  val PCDATA = "#PCDATA"
  val REM = "#REM"

  val SCHEMA = XSD_NAMESPACE+"schema"
  val COMPLEX_TYPE = XSD_NAMESPACE+"complexType"
  val SIMPLE_TYPE = XSD_NAMESPACE+"simpleType"
  val GROUP = XSD_NAMESPACE+"group"
  val SEQUENCE = XSD_NAMESPACE+"sequence"
  val ALL = XSD_NAMESPACE+"all"
  val XSD_CHOICE = XSD_NAMESPACE+"choice"
  val ELEMENT = XSD_NAMESPACE+"element"
  val ATTRIBUTE = XSD_NAMESPACE+"attribute"
  val ATTRIBUTE_GROUP = XSD_NAMESPACE+"attributeGroup"
  val ANNOTATION = XSD_NAMESPACE+"annotation"
  val APP_INFO = XSD_NAMESPACE+"appinfo"

  val DFDL_ASSERT = DFDL_NAMESPACE+"assert"
  val DFDL_CALENDAR_FORMAT = DFDL_NAMESPACE+"calendarFormat"
  val DFDL_CHOICE = DFDL_NAMESPACE+"choice"
  val DFDL_DISCRIMINATOR = DFDL_NAMESPACE+"discriminator"
  val DFDL_DEFINE_CALENDAR_FORMAT = DFDL_NAMESPACE+"defineCalendarFormat"
  val DFDL_DEFINE_FORMAT = DFDL_NAMESPACE+"defineFormat"
  val DFDL_DEFINE_ESCAPE_SCHEME = DFDL_NAMESPACE+"defineEscapeScheme"
  val DFDL_DEFINE_TEXT_NUMBER_FORMAT = DFDL_NAMESPACE+"defineTextNumberFormat"
  val DFDL_DEFINE_VARIABLE = DFDL_NAMESPACE+"defineVariable"
  val DFDL_ELEMENT = DFDL_NAMESPACE+"element"
  val DFDL_ESCAPE_SCHEMA = DFDL_NAMESPACE+"escapeScheme"
  val DFDL_FORMAT = DFDL_NAMESPACE+"format"
  val DFDL_GROUP = DFDL_NAMESPACE+"group"
  val DFDL_HIDDEN = DFDL_NAMESPACE+"hidden"
  val DFDL_NEW_VARIABLE_INSTANCE = DFDL_NAMESPACE+"newVariableInstance"
  val DFDL_PROPERTY = DFDL_NAMESPACE+"property"
  val DFDL_SET_VARIABLE = DFDL_NAMESPACE+"setVariable"
  val DFDL_SEQUENCE = DFDL_NAMESPACE+"sequence"
  val DFDL_SIMPLE_TYPE = DFDL_NAMESPACE+"simpleType"
  val DFDL_TEXT_NUMBER_FORMAT = DFDL_NAMESPACE+"textNumberFormat"
  val DFDL_RECURSIVE = DFDL_NAMESPACE+"recursive"


  //XSD data types

  val XSD_STRING = XSD_NAMESPACE+"string"
  val XSD_FLOAT = XSD_NAMESPACE+"float"
  val XSD_DOUBLE = XSD_NAMESPACE+"double"
  val XSD_DECIMAL = XSD_NAMESPACE+"decimal"
  val XSD_INTEGER = XSD_NAMESPACE+"integer"
  val XSD_LONG = XSD_NAMESPACE+"long"
  val XSD_INT = XSD_NAMESPACE+"int"
  val XSD_SHORT = XSD_NAMESPACE+"short"
  val XSD_BYTE = XSD_NAMESPACE+"byte"
  val XSD_UNSIGNED_LONG = XSD_NAMESPACE+"unsignedLong"
  val XSD_UNSIGNED_INT = XSD_NAMESPACE+"unsignedInt"
  val XSD_NON_NEGATIVE_INTEGER = XSD_NAMESPACE+"nonNegativeInteger"
  val XSD_UNSIGNED_SHORT = XSD_NAMESPACE+"unsignedShort"
  val XSD_UNSIGNED_BYTE = XSD_NAMESPACE+"unsignedByte"
  val XSD_BOOLEAN = XSD_NAMESPACE+"boolean"
  val XSD_DATE = XSD_NAMESPACE+"date"
  val XSD_TIME = XSD_NAMESPACE+"time"
  val XSD_DATE_TIME = XSD_NAMESPACE+"dateTime"
  val XSD_HEX_BINARY = XSD_NAMESPACE+"hexBinary"

  private val listPattern = Pattern.compile("'([^']*)'|([^'\\s]+)")

  private var shouldCompress = false
  private var hasCompressed = false

  def getFullName(e:Element) =
    e.getNamespaceURI() + e.getName()

  def getFullName(a:Attribute) =
    a.getNamespaceURI() + a.getName()

  def getAttribute(e:Element,name:String):Option[String] =
    e getAttributeValue(name) match {
      case null => None
      case s:String => Some(s)
    }

  def getFullName(name:String,namespaces:Namespaces) = {
    var namespace:String = ""
    var localName:String = ""

    if (name contains(":")){
      val parts = name split(":")
      namespace = namespaces getNamespaceURI(parts(0))
      localName = parts(1)
    }else{
      namespace = namespaces getNamespaceURI("")
      localName = name
    }
    namespace+localName
  }

  def getChildren(e:Element):Iterable[Element] = {
    e.getChildren.asInstanceOf[java.util.List[Element]]
  }

  def getAttributes(e:Element):Iterable[Attribute] = {
    e.getAttributes.asInstanceOf[java.util.List[Attribute]]
  }

  def getChildByName(parent:Element,name:String):Element = {
    for(child <- getChildren(parent))
      getAttribute(child,"name") match {
        case Some(s) => if (s==name) return child
        case _ =>
      }
    throw new DFDLSchemaDefinitionException("Reference not found:"+name,schemaContext = parent)
  }

  def getChildByTag(parent:Element,tag:String):Option[Element] = {
    for(child <- getChildren(parent))
      if (getFullName(child) == tag)
        return Some(child)
    None
  }

  def compare(x:Element,y:Element):Boolean = {
    val children1 = x getChildren
    val children2 = y getChildren;
    getFullName(x) == getFullName(y) && x.getText() == y.getText() &&
            children1.size == children2.size &&
            (0 until children1.size).forall { i:Int => compare(children1.get(i).asInstanceOf[Element],
              children2.get(i).asInstanceOf[Element]) }
  }

  def addNewChild(parent:Parent,name:String,target:String,namespaces:Namespaces) = {

    checkMemory(parent)

    val namespace = getTargetNamespace(name,target,namespaces)
    nodeCount += 1
    val ele = new CompressableElement(name,namespace)

    for( ns <- namespaces getNotNullNamespaces)
      ele addNamespaceDeclaration(ns)

    parent match {
      case null => ele
      case p:Document => p addContent(ele)
      case e:Element => e addContent(ele)
    }
    ele
  }

  def addNewChild(parent:Parent,name:String,value:String,target:String,namespaces:Namespaces) = {
    val namespace = getTargetNamespace(name,target,namespaces)

    checkMemory(parent)

    val ele = element(name,namespace,value)
    for( ns <- namespaces getNotNullNamespaces)
      ele addNamespaceDeclaration(ns)

    parent match {
      case null => ele
      case p:Document => p addContent(ele)
      case e:Element => e addContent(ele)
    }
    assert (ele.getDocument != null)
    ele
  }

  private def element(name:String,text:String) = {
    nodeCount += 1
    val ele = new CompressableElement(name)
    ele addContent(text)
    ele
  }

  private def element(name:String,namespace:Namespace,text:String) = {
    nodeCount += 1
    val ele = new CompressableElement(name,namespace)
    ele addContent(text)
    ele
  }

  private def element(name:String,children:List[Element]):Element = {    
    nodeCount += 1
    val node = new CompressableElement(name)
    children foreach { node addContent (_) }
    node
  }

  def removeChild(parent:Parent,child:Element):Unit = {
    if (child != null){
      val root = getRoot(child)
      if (parent != null)
        parent removeContent(child)
    }
  }

  def getRoot(child:Parent):Parent =
    if (child.getParent != null)
      getRoot(child getParent)
    else
      child


  def removeChildren(node:Parent,children:LinkedList[Element]):Unit =
    if (node != null && children !=null )
      children foreach { node removeContent (_) }


  def addChildren(parent:Parent,children:LinkedList[Element]):Unit =
    if (parent != null && children !=null )
     parent match {
      case p:Document => children foreach { p addContent(_) }
      case e:Element => children foreach { e addContent(_) }
    }
  
  def parse(is:InputStream):Document = {
	val builder = new SAXBuilder()
	builder.build(is)
  }
  
  def parse(f:File):Document = {
	  parse(new FileInputStream(f))
  }
  
  def serialize(parent:Parent) = {
    val sw = new StringWriter()
    val format = Format getPrettyFormat;
    format setLineSeparator(System.getProperty("line.separator"))
    parent match {
      case d:Document => new XMLOutputter(format).output(d,sw)
      case e:Element =>  new XMLOutputter(format).output(e,sw)
    }
    sw write(System.getProperty("line.separator"));
    sw toString
  }
  
  def serialize(out:OutputStream,parent:Parent) = {
    val format = Format getPrettyFormat;
    format setLineSeparator(System.getProperty("line.separator"))
    parent match {
      case d:Document => new XMLOutputter(format).output(d,out)
      case e:Element =>  new XMLOutputter(format).output(e,out)
    }
    out.write(System.getProperty("line.separator").getBytes())
    //new PrintWriter(out).println()
  }
  
  def getTotalNodes = nodeCount

  def printContext(element:Element,insert:String,context:Int):String = {
    try {
      val sw = new StringWriter()
      val separator = System.getProperty("line.separator")
      val writer = new daffodil.parser.xml.Writer(sw)
      val format = Format getPrettyFormat;
      format setLineSeparator(separator)
      getRoot(element) match {
        case document:Document => new daffodil.parser.xml.XMLOutputter(format).output(document,writer)
        case child:Element => new daffodil.parser.xml.XMLOutputter(format).output(child,writer)
      }
      val position = writer getPosition(element)
      val level = writer getLevel(element)
      val string = sw toString
      val indent = format getIndent
      var start = string lastIndexOf(separator,position-context)
      val stop = string indexOf(separator,position)
      var end = string indexOf(separator,position+context)

      start = if (start<0) 0 else start
      end = if (end<0) string.length else end

      val sb = new StringBuilder()
      sb append(string.substring(start,stop))
      sb append(separator)
      for( i <- 0 until level)
        sb append(indent)
      sb append(insert)
      sb append(string.substring(stop,end))

      sb toString
    }catch {
      case e:java.util.NoSuchElementException => "Unknown"
      case e:StringIndexOutOfBoundsException => "Unknown"
    }
  }

  def elem2Element(node:Node):Element = {
    val jdomNode = new CompressableElement(node label,node namespace)

    for(attribute <- node attributes)
      if (attribute.isPrefixed && attribute.getNamespace(node) != ""){
        println("THE ATTRIBUTE IS:"+attribute.key)
        println("THE NAMESPACE SHOULD BE:"+attribute.getNamespace(node))
        println("IT ACTUALLY IS:"+Namespace.getNamespace(attribute key,attribute getNamespace(node)))

        jdomNode setAttribute(attribute key,
                attribute.value.apply(0).text,
                Namespace getNamespace(node prefix,attribute getNamespace(node)))
      }
      else
        jdomNode setAttribute(attribute key, attribute.value.apply(0).text)

    for(child <- node child){
      child label match {
        case "#PCDATA" => jdomNode addContent(child toString)
        case "#REM" =>
        case _ => jdomNode addContent(elem2Element(child))
      }
    }
    jdomNode
  }

  private def map(c:Char) = c match {
    case 's' => ' '
    case 't' => '\t'
    case 'b' => '\b'
    case 'n' => '\n'
    case 'r' => '\r'
    case 'f' => '\f'
    case '\'' => '\''
    case '"' => '\"'
    case '\\' => '\\'
  }

  def getTargetNamespace(name:String,target:String,namespaces:Namespaces):Namespace = {
    if (name contains(":")){
      val parts = name split(":")
      namespaces getNamespaceByPrefix(parts(0)) match {
        case Some(n) => n
        case None => throw new DFDLSchemaDefinitionException("undefined prefix "+parts(0))
      }
    }else{
      namespaces addNamespace(target,"")
      namespaces.getNamespaceByPrefix("").get
    }
  }

  def getNamespaces(node:Element) = {
    val namespaces = new Namespaces
    namespaces addNamespaces (node)
    namespaces
  }

  def getNamespaces(node:Element,targetNamespace:String) = {
    val namespaces = new Namespaces
    namespaces addNamespaces (node)
    namespaces addNamespace (targetNamespace,null)
    namespaces
  }

  def getListFromValue(value:String):AttributeValue =
    if (XPathUtil isExpression(value))
      new ExpressionValue(value)
    else
      new ListLiteralValue(AnnotationParser.separate(value).map { AnnotationParser.unescape(_)})

  def getListFromExpression(expression:ExpressionValue,variables:VariableMap,
                            element:Parent,namespaces:Namespaces):List[Regex] =
    expression match {
      case ExpressionValue(e) =>
        XPathUtil.evalExpression(XPathUtil.getExpression(e),variables,element,namespaces) match {
          case StringResult(s) => AnnotationParser.separate(s).filter { _.length > 0 }. map { AnnotationParser unescape(_) }
          case NodeResult(n) => AnnotationParser.separate(n toString) .filter { _.length > 0 } map { AnnotationParser unescape(_) }
        }
    }


  private def getCompressablePath(node:Parent,path:List[CompressableNode]):List[CompressableNode] = {
    node getParent match {
      case e:CompressableNode => getCompressablePath(e,e::path)
      case _ => path
    }
  }

  private def getCompressableRoot(parent:Parent):CompressableNode =
    parent getParent match {
      case e:CompressableNode => getCompressableRoot(e)
      case _ => parent match {
        case e:CompressableNode => e
        case _ => throw new IllegalArgumentException("No compressable root for this node")
      }
    }


  private def compress(path:List[CompressableNode]) = {
    for(node <- path)
      node match {
        case p:CompressableElement =>
          if (! p.isCompressed)
            for(child <- p.getChildren)
               if (! path.contains(child))
                 child.asInstanceOf[CompressableNode].compress
        case _ => 
      }
  }

  def checkMemory(parent:Parent) = {
    if (nodeCount%100000 == 0 )
      DebugUtil log("Node count = "+nodeCount)

    if(shouldCompress){
      DebugUtil time ("Compressing",{
        val path = getCompressablePath(parent,Nil)
        compress(path)
        if (path.length == 0){
	        DebugUtil.log("Empty path "+path)
        }else{
          shouldCompress = false
          hasCompressed = true
        }
      })

      MemoryWarningSystem gc
      
      val mem = MemoryWarningSystem getMemoryUsage

      DebugUtil log("Serialization completed ("+MemoryWarningSystem.toMb(mem.getUsed)+"/"+
              MemoryWarningSystem.toMb(mem.getMax)+")")

      WARNING_MEMORY_PERCENTAGE += 0.025
      if (WARNING_MEMORY_PERCENTAGE>MAX_MEMORY_PERCENTAGE)
      	WARNING_MEMORY_PERCENTAGE = MAX_MEMORY_PERCENTAGE
      MemoryWarningSystem setPercentageUsageThreshold(WARNING_MEMORY_PERCENTAGE)
    }
  }

  MemoryWarningSystem setPercentageUsageThreshold(WARNING_MEMORY_PERCENTAGE)
  MemoryWarningSystem addListener { (used:Long,max:Long) =>
    shouldCompress = true;
   	DebugUtil log("Low memory condititon dectected ("+MemoryWarningSystem.toMb(used)+"/"+
             MemoryWarningSystem.toMb(max)+").") }

    
//  def getListFromExpression(expression:String,variables:VariableMap,
//                            element:Parent,namespaces:Namespaces):List[String] =
//    if (expression!=null)
//      if (XPathUtil isExpression(expression))
//        XPathUtil.evalExpression(expression,variables,element,namespaces) match {
//          case StringResult(s) => separate(s) filter { _.length > 0 }
//          case NodeResult(n) => separate(n toString) .filter { _.length > 0 }
//        }
//      else
//        separate(expression)
//    else
//      List()
}
