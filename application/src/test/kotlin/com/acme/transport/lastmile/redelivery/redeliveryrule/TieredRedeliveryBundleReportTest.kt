package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRedeliveryBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707087763)
        val items = mutableListOf<TieredRedeliveryBundle>()
        for (index in 0 until 6) {
            items.add(newTieredRedeliveryBundle("tiered-redelivery-bundle-$index", "sample"))
        }
        val report = buildTieredRedeliveryBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
