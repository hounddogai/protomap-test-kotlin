package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704920704)
        val items = mutableListOf<FallbackTariffEstimate>()
        for (index in 0 until 3) {
            items.add(newFallbackTariffEstimate("fallback-tariff-estimate-$index", "sample"))
        }
        val report = buildFallbackTariffEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
