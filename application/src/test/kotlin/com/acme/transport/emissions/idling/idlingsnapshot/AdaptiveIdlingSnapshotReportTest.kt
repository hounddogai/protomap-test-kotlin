package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIdlingSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707992876)
        val items = mutableListOf<AdaptiveIdlingSnapshot>()
        for (index in 0 until 5) {
            items.add(newAdaptiveIdlingSnapshot("adaptive-idling-snapshot-$index", "sample"))
        }
        val report = buildAdaptiveIdlingSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
