package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredClearanceQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701436508)
        val items = mutableListOf<TieredClearanceQuota>()
        for (index in 0 until 6) {
            items.add(newTieredClearanceQuota("tiered-clearance-quota-$index", "sample"))
        }
        val report = buildTieredClearanceQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
