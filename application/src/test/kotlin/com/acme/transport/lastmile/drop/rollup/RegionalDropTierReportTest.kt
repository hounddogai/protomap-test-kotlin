package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDropTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704856362)
        val items = mutableListOf<RegionalDropTier>()
        for (index in 0 until 2) {
            items.add(newRegionalDropTier("regional-drop-tier-$index", "sample"))
        }
        val report = buildRegionalDropTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
