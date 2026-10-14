package com.acme.transport.linehaul.relay.claim

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRelayAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700330339)
        val items = mutableListOf<TieredRelayAssignment>()
        for (index in 0 until 4) {
            items.add(newTieredRelayAssignment("tiered-relay-assignment-$index", "sample"))
        }
        val report = buildTieredRelayAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
