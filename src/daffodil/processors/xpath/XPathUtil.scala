package daffodil.processors.xpath

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

import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathVariableResolver
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathConstants.NODE
import javax.xml.xpath.XPathConstants.STRING
import javax.xml.namespace.QName

import org.jdom.Element
import org.jdom.Parent
import net.sf.saxon.om.NamespaceConstant
import net.sf.saxon.jdom.NodeWrapper
import net.sf.saxon.jdom.DocumentWrapper
import net.sf.saxon.Configuration
import net.sf.saxon.om.NodeInfo
import daffodil.xml.Namespaces
import daffodil.exceptions.XPathEvaluationException
import daffodil.processors.VariableMap

/**
 * Utility object for evaluating XPath expressions
 *
 * @version 1
 * @author Alejandro Rodriguez
 */
object XPathUtil {

  System.setProperty("javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_JDOM,"net.sf.saxon.xpath.XPathFactoryImpl")
  private val xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_JDOM)

  /**
   * Evaluates an XPath 2 expression
   *
   * @param a valid XPath expression the expression to evaluate (no surrounding brackets)
   * @param variables the variables in scope
   * @param contextNode the context node for this expression
   * @param namespaces the namespaces in scope
   */
  def evalExpression(expression:String,variables:VariableMap,
                     contextNode:Parent,namespaces:Namespaces):XPathResult = {


    val xpath = xpathFactory.newXPath()
    xpath.setXPathVariableResolver(
      new XPathVariableResolver() {
        def resolveVariable(qName:QName):Object =
          variables readVariable(qName.getNamespaceURI + qName.getLocalPart)
      })
    xpath setNamespaceContext(namespaces)


    try{
      val o = xpath.evaluate(expression,contextNode,NODE)
      new NodeResult(o.asInstanceOf[Element])
    }catch{
      case _:XPathExpressionException =>
        try {
          val o = xpath.evaluate(expression,contextNode,STRING)
          new StringResult(o.asInstanceOf[String])
        }catch {
          case e:XPathExpressionException => throw new XPathEvaluationException("Unknwon error evaluating '"+expression+"'",
            cause = e)
        }
      case e:Exception => throw new XPathEvaluationException("Unknwon error evaluating '"+expression+"'",cause = e)
    }
  }

  /**
   * Whether a string is a DFDL expression (an XPath expression surrounded by brackets).
   *
   * This function does not verify a string conforms to the DFDL subset of XPath
   */
  def isExpression(expression:String) =
    expression.startsWith("{") && expression.endsWith("}") &&
      (expression(1) != '{')

  /**
   * Returns the XPath expression contained in a DFDL expression (an XPath expression surrounded by brackets).
   *
   * @param expression a valid DFDL expression
   */
  def getExpression(expression:String) = {
    val v = expression.trim
    v.substring(1,v.length-1)
  }


}
