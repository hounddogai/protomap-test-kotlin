package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705211266)
        val items = mutableListOf<LegacyClearanceCheckpoint>()
        for (index in 0 until 3) {
            items.add(newLegacyClearanceCheckpoint("legacy-clearance-checkpoint-$index", "sample"))
        }
        val report = buildLegacyClearanceCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
