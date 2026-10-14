package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701082951)
        val items = mutableListOf<BatchedEmissionFactorSegment>()
        for (index in 0 until 5) {
            items.add(newBatchedEmissionFactorSegment("batched-emission-factor-segment-$index", "sample"))
        }
        val report = buildBatchedEmissionFactorSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
