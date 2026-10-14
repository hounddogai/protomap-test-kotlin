package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703687756)
        val items = mutableListOf<WeightedTariffThreshold>()
        for (index in 0 until 5) {
            items.add(newWeightedTariffThreshold("weighted-tariff-threshold-$index", "sample"))
        }
        val report = buildWeightedTariffThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
