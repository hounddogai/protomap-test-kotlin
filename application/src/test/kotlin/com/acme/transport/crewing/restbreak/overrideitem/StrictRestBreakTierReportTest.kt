package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700395849)
        val items = mutableListOf<StrictRestBreakTier>()
        for (index in 0 until 2) {
            items.add(newStrictRestBreakTier("strict-rest-break-tier-$index", "sample"))
        }
        val report = buildStrictRestBreakTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
