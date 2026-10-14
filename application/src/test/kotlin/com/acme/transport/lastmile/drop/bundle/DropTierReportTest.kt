package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702271953)
        val items = mutableListOf<DropTier>()
        for (index in 0 until 2) {
            items.add(newDropTier("drop-tier-$index", "sample"))
        }
        val report = buildDropTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
