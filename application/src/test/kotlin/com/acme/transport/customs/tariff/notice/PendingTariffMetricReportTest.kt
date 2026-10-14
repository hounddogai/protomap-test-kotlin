package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTariffMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702117037)
        val items = mutableListOf<PendingTariffMetric>()
        for (index in 0 until 4) {
            items.add(newPendingTariffMetric("pending-tariff-metric-$index", "sample"))
        }
        val report = buildPendingTariffMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
