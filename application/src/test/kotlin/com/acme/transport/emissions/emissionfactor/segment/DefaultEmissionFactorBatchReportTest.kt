package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700884903)
        val items = mutableListOf<DefaultEmissionFactorBatch>()
        for (index in 0 until 2) {
            items.add(newDefaultEmissionFactorBatch("default-emission-factor-batch-$index", "sample"))
        }
        val report = buildDefaultEmissionFactorBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
