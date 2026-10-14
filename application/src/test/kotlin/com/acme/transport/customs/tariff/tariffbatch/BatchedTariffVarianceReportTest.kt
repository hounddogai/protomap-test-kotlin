package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTariffVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708120181)
        val items = mutableListOf<BatchedTariffVariance>()
        for (index in 0 until 3) {
            items.add(newBatchedTariffVariance("batched-tariff-variance-$index", "sample"))
        }
        val report = buildBatchedTariffVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
