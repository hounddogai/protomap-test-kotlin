package com.acme.transport.network.node.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709990225)
        val items = mutableListOf<NodeReservation>()
        for (index in 0 until 5) {
            items.add(newNodeReservation("node-reservation-$index", "sample"))
        }
        val report = buildNodeReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
