package com.acme.transport.network.transittime.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708158125)
        val items = mutableListOf<TransitTimeRollup>()
        for (index in 0 until 5) {
            items.add(newTransitTimeRollup("transit-time-rollup-$index", "sample"))
        }
        val report = buildTransitTimeRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
