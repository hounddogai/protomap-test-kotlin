package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTariffMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702905053)
        val items = mutableListOf<AdaptiveTariffMetric>()
        for (index in 0 until 2) {
            items.add(newAdaptiveTariffMetric("adaptive-tariff-metric-$index", "sample"))
        }
        val report = buildAdaptiveTariffMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
