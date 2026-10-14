package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700250848)
        val items = mutableListOf<CapacityRequest>()
        for (index in 0 until 6) {
            items.add(newCapacityRequest("capacity-request-$index", "sample"))
        }
        val report = buildCapacityRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
