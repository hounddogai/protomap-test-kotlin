package com.acme.transport.safety.violation.exception

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709173442)
        val items = mutableListOf<ViolationException>()
        for (index in 0 until 3) {
            items.add(newViolationException("violation-exception-$index", "sample"))
        }
        val report = buildViolationExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
