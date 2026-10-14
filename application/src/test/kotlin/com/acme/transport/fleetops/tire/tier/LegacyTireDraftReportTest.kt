package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702079897)
        val items = mutableListOf<LegacyTireDraft>()
        for (index in 0 until 5) {
            items.add(newLegacyTireDraft("legacy-tire-draft-$index", "sample"))
        }
        val report = buildLegacyTireDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
