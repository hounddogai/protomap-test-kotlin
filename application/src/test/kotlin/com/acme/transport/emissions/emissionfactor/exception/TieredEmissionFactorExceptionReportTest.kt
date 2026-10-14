package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEmissionFactorExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702403568)
        val items = mutableListOf<TieredEmissionFactorException>()
        for (index in 0 until 6) {
            items.add(newTieredEmissionFactorException("tiered-emission-factor-exception-$index", "sample"))
        }
        val report = buildTieredEmissionFactorExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
