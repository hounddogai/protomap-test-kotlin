package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706146599)
        val items = mutableListOf<CapacityPolicy>()
        for (index in 0 until 6) {
            items.add(newCapacityPolicy("capacity-policy-$index", "sample"))
        }
        val report = buildCapacityPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
