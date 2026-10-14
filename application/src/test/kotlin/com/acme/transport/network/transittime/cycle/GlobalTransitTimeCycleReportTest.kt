package com.acme.transport.network.transittime.cycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTransitTimeCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707851328)
        val items = mutableListOf<GlobalTransitTimeCycle>()
        for (index in 0 until 3) {
            items.add(newGlobalTransitTimeCycle("global-transit-time-cycle-$index", "sample"))
        }
        val report = buildGlobalTransitTimeCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
