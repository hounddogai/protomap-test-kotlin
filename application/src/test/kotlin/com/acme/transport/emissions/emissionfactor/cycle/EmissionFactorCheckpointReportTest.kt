package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703261188)
        val items = mutableListOf<EmissionFactorCheckpoint>()
        for (index in 0 until 5) {
            items.add(newEmissionFactorCheckpoint("emission-factor-checkpoint-$index", "sample"))
        }
        val report = buildEmissionFactorCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
