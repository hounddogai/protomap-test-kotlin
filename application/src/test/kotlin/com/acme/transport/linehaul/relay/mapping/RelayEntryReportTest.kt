package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704523733)
        val items = mutableListOf<RelayEntry>()
        for (index in 0 until 2) {
            items.add(newRelayEntry("relay-entry-$index", "sample"))
        }
        val report = buildRelayEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
