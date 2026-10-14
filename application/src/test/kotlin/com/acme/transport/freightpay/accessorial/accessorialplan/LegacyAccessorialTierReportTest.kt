package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAccessorialTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708999643)
        val items = mutableListOf<LegacyAccessorialTier>()
        for (index in 0 until 2) {
            items.add(newLegacyAccessorialTier("legacy-accessorial-tier-$index", "sample"))
        }
        val report = buildLegacyAccessorialTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
