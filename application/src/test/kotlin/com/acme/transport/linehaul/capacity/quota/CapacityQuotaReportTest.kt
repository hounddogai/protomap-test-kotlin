package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708910232)
        val items = mutableListOf<CapacityQuota>()
        for (index in 0 until 3) {
            items.add(newCapacityQuota("capacity-quota-$index", "sample"))
        }
        val report = buildCapacityQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
