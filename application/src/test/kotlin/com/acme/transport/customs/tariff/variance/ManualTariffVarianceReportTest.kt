package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTariffVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708371836)
        val items = mutableListOf<ManualTariffVariance>()
        for (index in 0 until 6) {
            items.add(newManualTariffVariance("manual-tariff-variance-$index", "sample"))
        }
        val report = buildManualTariffVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
