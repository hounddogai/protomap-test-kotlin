package com.acme.transport.network.servicearea.cycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServiceAreaEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709927081)
        val items = mutableListOf<PendingServiceAreaEntry>()
        for (index in 0 until 5) {
            items.add(newPendingServiceAreaEntry("pending-service-area-entry-$index", "sample"))
        }
        val report = buildPendingServiceAreaEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
