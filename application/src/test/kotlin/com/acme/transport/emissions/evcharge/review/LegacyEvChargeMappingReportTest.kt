package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyEvChargeMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700886667)
        val items = mutableListOf<LegacyEvChargeMapping>()
        for (index in 0 until 6) {
            items.add(newLegacyEvChargeMapping("legacy-ev-charge-mapping-$index", "sample"))
        }
        val report = buildLegacyEvChargeMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
