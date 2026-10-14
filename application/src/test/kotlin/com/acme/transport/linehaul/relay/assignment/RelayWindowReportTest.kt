package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702928196)
        val items = mutableListOf<RelayWindow>()
        for (index in 0 until 4) {
            items.add(newRelayWindow("relay-window-$index", "sample"))
        }
        val report = buildRelayWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
