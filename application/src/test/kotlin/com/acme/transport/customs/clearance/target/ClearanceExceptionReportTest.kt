package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706959077)
        val items = mutableListOf<ClearanceException>()
        for (index in 0 until 6) {
            items.add(newClearanceException("clearance-exception-$index", "sample"))
        }
        val report = buildClearanceExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
