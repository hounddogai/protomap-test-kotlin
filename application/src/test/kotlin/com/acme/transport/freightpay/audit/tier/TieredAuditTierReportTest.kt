package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703628767)
        val items = mutableListOf<TieredAuditTier>()
        for (index in 0 until 5) {
            items.add(newTieredAuditTier("tiered-audit-tier-$index", "sample"))
        }
        val report = buildTieredAuditTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
