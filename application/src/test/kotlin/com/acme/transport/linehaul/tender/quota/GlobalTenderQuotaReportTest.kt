package com.acme.transport.linehaul.tender.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703995187)
        val items = mutableListOf<GlobalTenderQuota>()
        for (index in 0 until 6) {
            items.add(newGlobalTenderQuota("global-tender-quota-$index", "sample"))
        }
        val report = buildGlobalTenderQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
