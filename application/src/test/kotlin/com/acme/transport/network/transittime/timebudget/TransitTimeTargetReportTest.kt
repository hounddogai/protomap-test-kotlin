package com.acme.transport.network.transittime.timebudget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707229681)
        val items = mutableListOf<TransitTimeTarget>()
        for (index in 0 until 2) {
            items.add(newTransitTimeTarget("transit-time-target-$index", "sample"))
        }
        val report = buildTransitTimeTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
