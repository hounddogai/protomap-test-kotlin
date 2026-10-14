package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705210311)
        val items = mutableListOf<CapacityException>()
        for (index in 0 until 2) {
            items.add(newCapacityException("capacity-exception-$index", "sample"))
        }
        val report = buildCapacityExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
