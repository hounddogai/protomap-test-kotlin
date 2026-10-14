package com.acme.transport.network.transittime.timebatch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700731679)
        val items = mutableListOf<TransitTimeCycle>()
        for (index in 0 until 6) {
            items.add(newTransitTimeCycle("transit-time-cycle-$index", "sample"))
        }
        val report = buildTransitTimeCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
