package daffodil

import daffodil.debugger.DebugUtil
import daffodil.parser.SchemaParser
import xml.XMLUtil
import java.io.FileOutputStream
import java.io.FileInputStream
import daffodil.xml.TransformUtil
import scala.io.Source
import org.scalatest.junit.JUnit3Suite
import junit.framework.Assert._
import scala.xml.Utility.trim

class StandAloneTests extends JUnit3Suite {
  
  val isDebug = false
  
//  def testStandAloneTests() {
//    val allLines = Source.fromFile("test/testSuite.txt").getLines.toList
//    val testLines = allLines.filter { !_.startsWith("#") } //strip comments
//    for (line <- testLines) {
//      val p = line.split(",")
//      val testDir = "test/"
//      val (schemaFileName, root, inputFileName, expectedFileName) = (testDir+p(0), p(1), testDir+p(2), testDir+p(3))
//      val schemaParser = new SchemaParser
//      if (isDebug)
//        schemaParser setDebugging (true)
//      System.err.println("Test "+ schemaFileName)
//      DebugUtil.time("Parsing schema", schemaParser parse (schemaFileName))
//      val result = schemaParser eval (inputFileName, root)
//      val res = trim(XMLUtil.element2Elem(result))
//
//      DebugUtil log ("Total nodes:" + XMLUtil.getTotalNodes)
//      val expectedXML = trim(scala.xml.XML.loadFile(expectedFileName))
//      assertEquals(expectedXML, res) // Need to compare in a canonicalized manner.
//
//    }
  
  def doTest(schemaFileName : String, rootName : String, inputFileName : String, expectedFileName : String) {
      val schemaParser = new SchemaParser
      if (isDebug)
        schemaParser setDebugging (true)
      val testDir = "test/"
      System.err.print("\nTest "+ inputFileName)
      DebugUtil.time("Parsing schema", schemaParser parse (testDir + schemaFileName))
      val result = schemaParser eval (testDir + inputFileName, rootName)
      val res = trim(XMLUtil.element2Elem(result))

      System.err.print(". Total nodes:" + XMLUtil.getTotalNodes)
      val expectedXML = trim(scala.xml.XML.loadFile(testDir + expectedFileName))
      assertEquals(expectedXML, res) // Need to compare in a canonicalized manner.
      System.err.print(" passed.")
  }
  

def testAA000() { doTest("AA.xsd", "list", "AA000.in", "AA000.xml") }
def testAB000() { doTest("AB.xsd", "matrix", "AB000.in", "AB000.xml") }
def testAB001() { doTest("AB.xsd", "matrix", "AB001.in", "AB000.xml") }
// def testAB002() { doTest("AB.xsd", "matrix", "AB002.in", "AB000.xml") }
def testAB003() { doTest("AB.xsd", "matrix", "AB003.in", "AB003.xml") }
// def testAB004() { doTest("AB.xsd", "matrix", "AB004.in", "AB004.xml") }
// def testAB005() { doTest("AB.xsd", "matrix", "AB005.in", "AB005.xml") }
// def testAB010() { doTest("AB.xsd", "matrix", "AB010.in", "AB010.xml") }
// Hidden layers def testAC000() { doTest("AC.xsd", "table", "AC000.in", "AC000.xml") }
// Hidden layers def testAD000() { doTest("AD.xsd", "list", "AD000.in", "AD000.xml") }
// def testAE000() { doTest("AE.xsd", "transposedMatrix", "AE000.in", "AE000.xml") } // non-standard multi-assignment
def testAF000() { doTest("AF.xsd", "allZones", "AF000.in", "AF000.xml") }
def testAF001() { doTest("AF.xsd", "allZones", "AF001.in", "AF001.xml") }
def testAF002() { doTest("AF.xsd", "allZones", "AF002.in", "AF002.xml") }
// Hidden layers def testAG000() { doTest("AG.xsd", "allZones", "AG000.in", "AG000.xml") }
// Hidden layers def testAG001() { doTest("AG.xsd", "allZones", "AG001.in", "AG001.xml") }
// Hidden layers def testAG002() { doTest("AG.xsd", "allZones", "AG002.in", "AG002.xml") }
def testAH000() { doTest("AH.xsd", "allZones", "AH000.in", "AH000.xml") }
def testAH001() { doTest("AH.xsd", "allZones", "AH001.in", "AH001.xml") }
def testAH002() { doTest("AH.xsd", "allZones", "AH002.in", "AH002.xml") }
def testAI000() { doTest("AI.xsd", "list", "AI000.in", "AI000.xml") }
def testAJ000() { doTest("AJ.xsd", "list", "AJ000.in", "AJ000.xml") }
def testAJ001() { doTest("AJ.xsd", "list", "AJ001.in", "AJ001.xml") }
def testAK000() { doTest("AK.xsd", "list", "AK000.in", "AK000.xml") }
def testAK001() { doTest("AK.xsd", "list", "AK001.in", "AK001.xml") }
def testAL000() { doTest("AL.xsd", "list", "AL000.in", "AL000.xml") }
// Hidden Layers def testAM000() { doTest("AM.xsd", "mimeType", "AM000.in", "AM000.xml") }
// Hidden Layers def testAM001() { doTest("AM.xsd", "mimeType", "AM001.in", "AM001.xml") }
def testAN000() { doTest("AN.xsd", "path", "AN000.in", "AN000.xml") }
def testAN001() { doTest("AN.xsd", "path", "AN001.in", "AN001.xml") }
// hidden layers def testAO000() { doTest("AO.xsd", "element", "AO000.in", "AO000.xml") }
// hidden layers def testAO001() { doTest("AO.xsd", "element", "AO001.in", "AO001.xml") }
// hidden layers def testAO002() { doTest("AO.xsd", "element", "AO002.in", "AO002.xml") }
// hidden layers def testAO003() { doTest("AO.xsd", "element", "AO003.in", "AO003.xml") }
// hidden layers def testAO004() { doTest("AO.xsd", "element", "AO004.in", "AO004.xml") }
def testAP000() { doTest("AP.xsd", "parent", "AP000.in", "AP000.xml") }
def testAQ000() { doTest("AQ.xsd", "ROOT", "AQ000.in", "AQ000.xml") }
def testAR000() { doTest("AR.xsd", "DFDL", "AR000.in", "AR000.xml") }
// hidden layers def testAS000() { doTest("AS.xsd", "table", "AS000.in", "AS000.xml") }
// hidden layers def testAT000() { doTest("AT.xsd", "PRP", "AT000.in", "AT000.xml") }
def testAU000() { doTest("AU.xsd", "list", "AU000.in", "AU000.xml") }
// hidden layers def testAV000() { doTest("AV.xsd", "wholeFile", "AV000.in", "AV000.xml") }
// hidden layers def testAV001() { doTest("AV.xsd", "wholeFile", "AV001.in", "AV001.xml") }
// hidden layers def testAV002() { doTest("AV.xsd", "wholeFile", "AV002.in", "AV002.xml") }
// hidden layers def testAV003() { doTest("AV.xsd", "wholeFile", "AV003.in", "AV003.xml") }
def testAW000() { doTest("AW.xsd", "list", "AW000.in", "AW000.xml") }
def testAW001() { doTest("AW.xsd", "list", "AW001.in", "AW001.xml") }
def testAX000() { doTest("AX.xsd", "list", "AX000.in", "AX000.xml") }
def testAZ000() { doTest("AZ.xsd", "list", "AZ000.in", "AZ000.xml") }
def testBA000() { doTest("BA.xsd", "list", "BA000.in", "BA000.xml") }
def testBB000() { doTest("BB.xsd", "list", "BB000.in", "BB000.xml") }
def testBC000() { doTest("BC.xsd", "list", "BC000.in", "BC000.xml") }
def testBD000() { doTest("BD.xsd", "list", "BD000.in", "BD000.xml") }
def testBE000() { doTest("BE.xsd", "seq", "BE000.in", "BE000.xml") }
def testBE001() { doTest("BE.xsd", "seq", "BE001.in", "BE001.xml") }
def testBF000() { doTest("BF.xsd", "root", "BF000.in", "BF000.xml") }
def testBF001() { doTest("BF.xsd", "root", "BF001.in", "BF001.xml") }
def testBG000() { doTest("BG.xsd", "list", "BG000.in", "BG000.xml") }

}