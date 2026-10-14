package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707310815)
        val items = mutableListOf<StrictSwapCheckpoint>()
        for (index in 0 until 5) {
            items.add(newStrictSwapCheckpoint("strict-swap-checkpoint-$index", "sample"))
        }
        val report = buildStrictSwapCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
