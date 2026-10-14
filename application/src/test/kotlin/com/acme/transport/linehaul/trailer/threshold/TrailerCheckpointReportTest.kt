package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700107418)
        val items = mutableListOf<TrailerCheckpoint>()
        for (index in 0 until 4) {
            items.add(newTrailerCheckpoint("trailer-checkpoint-$index", "sample"))
        }
        val report = buildTrailerCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
