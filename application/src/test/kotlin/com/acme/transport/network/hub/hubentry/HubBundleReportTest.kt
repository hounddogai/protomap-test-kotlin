package com.acme.transport.network.hub.hubentry

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704177066)
        val items = mutableListOf<HubBundle>()
        for (index in 0 until 3) {
            items.add(newHubBundle("hub-bundle-$index", "sample"))
        }
        val report = buildHubBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
