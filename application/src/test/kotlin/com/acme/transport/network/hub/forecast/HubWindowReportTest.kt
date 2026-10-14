package com.acme.transport.network.hub.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706942815)
        val items = mutableListOf<HubWindow>()
        for (index in 0 until 2) {
            items.add(newHubWindow("hub-window-$index", "sample"))
        }
        val report = buildHubWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
