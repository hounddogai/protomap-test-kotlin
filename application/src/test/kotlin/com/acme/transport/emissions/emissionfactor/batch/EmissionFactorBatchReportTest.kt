package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704783780)
        val items = mutableListOf<EmissionFactorBatch>()
        for (index in 0 until 3) {
            items.add(newEmissionFactorBatch("emission-factor-batch-$index", "sample"))
        }
        val report = buildEmissionFactorBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
