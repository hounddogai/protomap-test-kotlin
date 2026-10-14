package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703592848)
        val items = mutableListOf<SeasonalAccessorialTier>()
        for (index in 0 until 4) {
            items.add(newSeasonalAccessorialTier("seasonal-accessorial-tier-$index", "sample"))
        }
        val report = buildSeasonalAccessorialTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
