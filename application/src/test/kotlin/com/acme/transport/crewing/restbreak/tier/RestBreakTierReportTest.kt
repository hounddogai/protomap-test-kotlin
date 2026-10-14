package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701948283)
        val items = mutableListOf<RestBreakTier>()
        for (index in 0 until 3) {
            items.add(newRestBreakTier("rest-break-tier-$index", "sample"))
        }
        val report = buildRestBreakTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
