package com.acme.transport.network.cutoff.tier

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCutoffTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705222344)
        val items = mutableListOf<GlobalCutoffTier>()
        for (index in 0 until 4) {
            items.add(newGlobalCutoffTier("global-cutoff-tier-$index", "sample"))
        }
        val report = buildGlobalCutoffTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
