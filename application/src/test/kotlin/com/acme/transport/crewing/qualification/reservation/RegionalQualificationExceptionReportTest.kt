package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702365907)
        val items = mutableListOf<RegionalQualificationException>()
        for (index in 0 until 2) {
            items.add(newRegionalQualificationException("regional-qualification-exception-$index", "sample"))
        }
        val report = buildRegionalQualificationExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
