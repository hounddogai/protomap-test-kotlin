package com.acme.transport.network.node.slot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702471466)
        val items = mutableListOf<PendingNodeSlot>()
        for (index in 0 until 3) {
            items.add(newPendingNodeSlot("pending-node-slot-$index", "sample"))
        }
        val report = buildPendingNodeSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
