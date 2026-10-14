package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708939637)
        val items = mutableListOf<FallbackLanePlanCheckpoint>()
        for (index in 0 until 5) {
            items.add(newFallbackLanePlanCheckpoint("fallback-lane-plan-checkpoint-$index", "sample"))
        }
        val report = buildFallbackLanePlanCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
