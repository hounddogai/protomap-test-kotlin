package com.acme.transport.network.zone.claim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackZoneThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703168805)
        val items = mutableListOf<FallbackZoneThreshold>()
        for (index in 0 until 4) {
            items.add(newFallbackZoneThreshold("fallback-zone-threshold-$index", "sample"))
        }
        val report = buildFallbackZoneThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
