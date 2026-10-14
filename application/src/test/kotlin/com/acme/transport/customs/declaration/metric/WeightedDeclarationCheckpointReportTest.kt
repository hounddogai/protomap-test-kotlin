package com.acme.transport.customs.declaration.metric

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDeclarationCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701221162)
        val items = mutableListOf<WeightedDeclarationCheckpoint>()
        for (index in 0 until 5) {
            items.add(newWeightedDeclarationCheckpoint("weighted-declaration-checkpoint-$index", "sample"))
        }
        val report = buildWeightedDeclarationCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
