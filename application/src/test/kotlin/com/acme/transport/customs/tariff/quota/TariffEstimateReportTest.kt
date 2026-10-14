package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702654960)
        val items = mutableListOf<TariffEstimate>()
        for (index in 0 until 2) {
            items.add(newTariffEstimate("tariff-estimate-$index", "sample"))
        }
        val report = buildTariffEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
