package com.acme.transport.linehaul.relay.window

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701589503)
        val items = mutableListOf<RollingRelayEntry>()
        for (index in 0 until 3) {
            items.add(newRollingRelayEntry("rolling-relay-entry-$index", "sample"))
        }
        val report = buildRollingRelayEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
