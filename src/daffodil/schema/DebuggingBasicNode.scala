package daffodil.schema

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
import org.jdom.Parent

import daffodil.debugger.DebuggingListener
import daffodil.processors.VariableMap
import daffodil.schema.annotation.Annotation
import daffodil.xml.Namespaces
import daffodil.parser.{LinkedList, RollbackStream}
import daffodil.parser.regex.Regex

@SerialVersionUID(1)
class DebuggingBasicNode(ann:Annotation,schemaContext:org.jdom.Element,
                         debugger:DebuggingListener,
                         namespaces:Namespaces,node:BasicNode) extends BasicNode(null,namespaces,ann) {
  
   override def apply(input:RollbackStream,parentAnnotations:Annotation,
                     variables:VariableMap,parent:Parent,
                     maxLength:Int,terminators:List[Regex]):LinkedList[org.jdom.Element] = {
     debugger step(schemaContext,ann,parent,variables,namespaces,input)
     node(input,parentAnnotations,variables,parent,maxLength,terminators)
  }
}
