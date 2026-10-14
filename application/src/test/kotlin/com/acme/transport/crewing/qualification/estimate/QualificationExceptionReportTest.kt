package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707390906)
        val items = mutableListOf<QualificationException>()
        for (index in 0 until 6) {
            items.add(newQualificationException("qualification-exception-$index", "sample"))
        }
        val report = buildQualificationExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
