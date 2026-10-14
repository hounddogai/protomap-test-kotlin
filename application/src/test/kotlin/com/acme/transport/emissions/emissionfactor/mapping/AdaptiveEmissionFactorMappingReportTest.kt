package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706943718)
        val items = mutableListOf<AdaptiveEmissionFactorMapping>()
        for (index in 0 until 5) {
            items.add(newAdaptiveEmissionFactorMapping("adaptive-emission-factor-mapping-$index", "sample"))
        }
        val report = buildAdaptiveEmissionFactorMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
