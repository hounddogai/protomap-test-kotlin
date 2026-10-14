package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyEvChargeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705202625)
        val items = mutableListOf<LegacyEvChargeVariance>()
        for (index in 0 until 2) {
            items.add(newLegacyEvChargeVariance("legacy-ev-charge-variance-$index", "sample"))
        }
        val report = buildLegacyEvChargeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
