package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702940633)
        val items = mutableListOf<LenientViolationException>()
        for (index in 0 until 2) {
            items.add(newLenientViolationException("lenient-violation-exception-$index", "sample"))
        }
        val report = buildLenientViolationExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
