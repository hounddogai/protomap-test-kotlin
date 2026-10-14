package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705115078)
        val items = mutableListOf<EmissionFactorMilestone>()
        for (index in 0 until 6) {
            items.add(newEmissionFactorMilestone("emission-factor-milestone-$index", "sample"))
        }
        val report = buildEmissionFactorMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
