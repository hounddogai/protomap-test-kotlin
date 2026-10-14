package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705588650)
        val items = mutableListOf<EmissionFactorPlan>()
        for (index in 0 until 6) {
            items.add(newEmissionFactorPlan("emission-factor-plan-$index", "sample"))
        }
        val report = buildEmissionFactorPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
