package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702466556)
        val items = mutableListOf<ScheduledRestBreakCheckpoint>()
        for (index in 0 until 6) {
            items.add(newScheduledRestBreakCheckpoint("scheduled-rest-break-checkpoint-$index", "sample"))
        }
        val report = buildScheduledRestBreakCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
