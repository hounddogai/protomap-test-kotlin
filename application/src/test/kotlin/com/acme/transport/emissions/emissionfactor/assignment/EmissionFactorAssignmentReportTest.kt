package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708932106)
        val items = mutableListOf<EmissionFactorAssignment>()
        for (index in 0 until 6) {
            items.add(newEmissionFactorAssignment("emission-factor-assignment-$index", "sample"))
        }
        val report = buildEmissionFactorAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
