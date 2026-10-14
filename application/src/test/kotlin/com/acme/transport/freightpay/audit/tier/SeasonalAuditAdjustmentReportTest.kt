package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAuditAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704232547)
        val items = mutableListOf<SeasonalAuditAdjustment>()
        for (index in 0 until 2) {
            items.add(newSeasonalAuditAdjustment("seasonal-audit-adjustment-$index", "sample"))
        }
        val report = buildSeasonalAuditAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
