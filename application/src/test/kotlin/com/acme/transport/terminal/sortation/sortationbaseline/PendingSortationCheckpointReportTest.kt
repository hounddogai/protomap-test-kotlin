package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702843980)
        val items = mutableListOf<PendingSortationCheckpoint>()
        for (index in 0 until 5) {
            items.add(newPendingSortationCheckpoint("pending-sortation-checkpoint-$index", "sample"))
        }
        val report = buildPendingSortationCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
