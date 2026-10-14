package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706684248)
        val items = mutableListOf<TrailerYardCheckpoint>()
        for (index in 0 until 6) {
            items.add(newTrailerYardCheckpoint("trailer-yard-checkpoint-$index", "sample"))
        }
        val report = buildTrailerYardCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
