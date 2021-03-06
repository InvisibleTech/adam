/**
 * Licensed to Big Data Genomics (BDG) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The BDG licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.adam.converters

import htsjdk.samtools._
import java.io.File
import org.scalatest.FunSuite
import scala.collection.JavaConversions._

class SAMRecordConverterSuite extends FunSuite {

  test("testing the fields in an Alignment obtained from a mapped samRecord conversion") {

    val testRecordConverter = new SAMRecordConverter
    val testFileString = getClass.getClassLoader.getResource("reads12.sam").getFile
    val testFile = new File(testFileString)

    // Iterator of SamReads in the file that each have a samRecord for conversion
    val testIterator = SamReaderFactory.makeDefault().open(testFile)
    val testSAMRecord = testIterator.iterator().next()

    // set the oq, md, oc, and op attributes
    testSAMRecord.setOriginalBaseQualities("*****".getBytes.map(v => (v - 33).toByte))
    testSAMRecord.setAttribute("MD", "100")
    testSAMRecord.setAttribute("OC", "100M")
    testSAMRecord.setAttribute("OP", 1)

    // Convert samRecord to Alignment
    val testAlignment = testRecordConverter.convert(testSAMRecord)

    // Validating Conversion
    assert(testAlignment.getCigar === testSAMRecord.getCigarString)
    assert(testAlignment.getDuplicateRead === testSAMRecord.getDuplicateReadFlag)
    assert(testAlignment.getEnd.toInt === testSAMRecord.getAlignmentEnd)
    assert(testAlignment.getMappingQuality.toInt === testSAMRecord.getMappingQuality)
    assert(testAlignment.getStart.toInt === (testSAMRecord.getAlignmentStart - 1))
    assert(testAlignment.getReadInFragment == 0)
    assert(testAlignment.getFailedVendorQualityChecks === testSAMRecord.getReadFailsVendorQualityCheckFlag)
    assert(!testAlignment.getPrimaryAlignment === testSAMRecord.getNotPrimaryAlignmentFlag)
    assert(!testAlignment.getReadMapped === testSAMRecord.getReadUnmappedFlag)
    assert(testAlignment.getReadName === testSAMRecord.getReadName)
    assert(testAlignment.getReadNegativeStrand === testSAMRecord.getReadNegativeStrandFlag)
    assert(!testAlignment.getReadPaired)
    assert(testAlignment.getReadInFragment != 1)
    assert(testAlignment.getSupplementaryAlignment === testSAMRecord.getSupplementaryAlignmentFlag)
    assert(testAlignment.getOriginalQualityScores === "*****")
    assert(testAlignment.getMismatchingPositions === "100")
    assert(testAlignment.getOriginalCigar === "100M")
    assert(testAlignment.getOriginalStart === 0L)
    assert(testAlignment.getAttributes === "XS:i:0\tAS:i:75\tNM:i:0")
  }

  test("testing the fields in an Alignment obtained from an unmapped samRecord conversion") {

    val testRecordConverter = new SAMRecordConverter
    val testFileString = getClass.getClassLoader.getResource("reads12.sam").getFile
    val testFile = new File(testFileString)

    // Iterator of SamReads in the file that each have a samRecord for conversion
    val testIterator = SamReaderFactory.makeDefault().open(testFile)
    val testSAMRecord = testIterator.iterator().next()

    // Convert samRecord to Alignment
    val testAlignment = testRecordConverter.convert(testSAMRecord)

    // Validating Conversion
    assert(testAlignment.getCigar === testSAMRecord.getCigarString)
    assert(testAlignment.getDuplicateRead === testSAMRecord.getDuplicateReadFlag)
    assert(testAlignment.getEnd.toInt === testSAMRecord.getAlignmentEnd)
    assert(testAlignment.getMappingQuality.toInt === testSAMRecord.getMappingQuality)
    assert(testAlignment.getStart.toInt === (testSAMRecord.getAlignmentStart - 1))
    assert(testAlignment.getReadInFragment == 0)
    assert(testAlignment.getFailedVendorQualityChecks === testSAMRecord.getReadFailsVendorQualityCheckFlag)
    assert(!testAlignment.getPrimaryAlignment === testSAMRecord.getNotPrimaryAlignmentFlag)
    assert(!testAlignment.getReadMapped === testSAMRecord.getReadUnmappedFlag)
    assert(testAlignment.getReadName === testSAMRecord.getReadName)
    assert(testAlignment.getReadNegativeStrand === testSAMRecord.getReadNegativeStrandFlag)
    assert(!testAlignment.getReadPaired)
    assert(testAlignment.getReadInFragment != 1)
    assert(testAlignment.getSupplementaryAlignment === testSAMRecord.getSupplementaryAlignmentFlag)
  }

  test("'*' quality gets nulled out") {

    val newRecordConverter = new SAMRecordConverter
    val newTestFile = new File(getClass.getClassLoader.getResource("unmapped.sam").getFile)
    val newSAMReader = SamReaderFactory.makeDefault().open(newTestFile)

    // Obtain SAMRecord
    val newSAMRecordIter = {
      val samIter = asScalaIterator(newSAMReader.iterator())
      samIter.toIterable.dropWhile(!_.getReadUnmappedFlag)
    }
    val newSAMRecord = newSAMRecordIter.toIterator.next()

    // null out quality
    newSAMRecord.setBaseQualityString("*")

    // Conversion
    val newAlignment = newRecordConverter.convert(newSAMRecord)

    // Validating Conversion
    assert(newAlignment.getQualityScores === null)
  }

  test("don't keep denormalized fields") {
    val rc = new SAMRecordConverter

    assert(rc.skipTag("MD"))
    assert(rc.skipTag("OQ"))
    assert(rc.skipTag("OP"))
    assert(rc.skipTag("OC"))
  }
}
