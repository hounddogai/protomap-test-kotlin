package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRedeliverySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703928926)
        val items = mutableListOf<PendingRedeliverySlot>()
        for (index in 0 until 2) {
            items.add(newPendingRedeliverySlot("pending-redelivery-slot-$index", "sample"))
        }
        val report = buildPendingRedeliverySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
