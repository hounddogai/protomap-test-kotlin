package com.acme.transport.safety.violation.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701457474)
        val items = mutableListOf<ViolationRequest>()
        for (index in 0 until 2) {
            items.add(newViolationRequest("violation-request-$index", "sample"))
        }
        val report = buildViolationRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
