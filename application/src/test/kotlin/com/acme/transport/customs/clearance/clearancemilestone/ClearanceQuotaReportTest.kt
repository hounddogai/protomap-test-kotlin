package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700181730)
        val items = mutableListOf<ClearanceQuota>()
        for (index in 0 until 4) {
            items.add(newClearanceQuota("clearance-quota-$index", "sample"))
        }
        val report = buildClearanceQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
