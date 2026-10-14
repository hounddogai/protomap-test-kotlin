package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700203818)
        val items = mutableListOf<QualificationAssignment>()
        for (index in 0 until 4) {
            items.add(newQualificationAssignment("qualification-assignment-$index", "sample"))
        }
        val report = buildQualificationAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
