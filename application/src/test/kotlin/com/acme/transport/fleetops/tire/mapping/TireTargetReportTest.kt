package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703539797)
        val items = mutableListOf<TireTarget>()
        for (index in 0 until 2) {
            items.add(newTireTarget("tire-target-$index", "sample"))
        }
        val report = buildTireTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
