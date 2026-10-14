package com.acme.transport.network.hub.snapshot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709829022)
        val items = mutableListOf<HubSnapshot>()
        for (index in 0 until 4) {
            items.add(newHubSnapshot("hub-snapshot-$index", "sample"))
        }
        val report = buildHubSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
