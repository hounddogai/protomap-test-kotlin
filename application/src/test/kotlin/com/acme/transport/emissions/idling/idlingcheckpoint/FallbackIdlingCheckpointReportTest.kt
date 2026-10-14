package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708845258)
        val items = mutableListOf<FallbackIdlingCheckpoint>()
        for (index in 0 until 2) {
            items.add(newFallbackIdlingCheckpoint("fallback-idling-checkpoint-$index", "sample"))
        }
        val report = buildFallbackIdlingCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
