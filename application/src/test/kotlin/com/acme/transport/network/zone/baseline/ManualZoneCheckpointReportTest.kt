package com.acme.transport.network.zone.baseline

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualZoneCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707385410)
        val items = mutableListOf<ManualZoneCheckpoint>()
        for (index in 0 until 4) {
            items.add(newManualZoneCheckpoint("manual-zone-checkpoint-$index", "sample"))
        }
        val report = buildManualZoneCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
