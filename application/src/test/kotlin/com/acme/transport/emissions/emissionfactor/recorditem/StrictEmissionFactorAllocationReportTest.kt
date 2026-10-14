package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEmissionFactorAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709269550)
        val items = mutableListOf<StrictEmissionFactorAllocation>()
        for (index in 0 until 2) {
            items.add(newStrictEmissionFactorAllocation("strict-emission-factor-allocation-$index", "sample"))
        }
        val report = buildStrictEmissionFactorAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
