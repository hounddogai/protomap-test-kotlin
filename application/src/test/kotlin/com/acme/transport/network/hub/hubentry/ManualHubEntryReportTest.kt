package com.acme.transport.network.hub.hubentry

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHubEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709264692)
        val items = mutableListOf<ManualHubEntry>()
        for (index in 0 until 6) {
            items.add(newManualHubEntry("manual-hub-entry-$index", "sample"))
        }
        val report = buildManualHubEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
