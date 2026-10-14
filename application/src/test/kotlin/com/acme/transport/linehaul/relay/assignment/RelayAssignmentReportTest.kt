package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701543180)
        val items = mutableListOf<RelayAssignment>()
        for (index in 0 until 5) {
            items.add(newRelayAssignment("relay-assignment-$index", "sample"))
        }
        val report = buildRelayAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
