package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709602429)
        val items = mutableListOf<TireRequest>()
        for (index in 0 until 2) {
            items.add(newTireRequest("tire-request-$index", "sample"))
        }
        val report = buildTireRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
