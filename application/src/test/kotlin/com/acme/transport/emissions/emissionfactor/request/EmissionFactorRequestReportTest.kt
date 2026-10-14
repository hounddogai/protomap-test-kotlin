package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705434035)
        val items = mutableListOf<EmissionFactorRequest>()
        for (index in 0 until 6) {
            items.add(newEmissionFactorRequest("emission-factor-request-$index", "sample"))
        }
        val report = buildEmissionFactorRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
