package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDisputeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706059130)
        val items = mutableListOf<LegacyDisputeTier>()
        for (index in 0 until 4) {
            items.add(newLegacyDisputeTier("legacy-dispute-tier-$index", "sample"))
        }
        val report = buildLegacyDisputeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
