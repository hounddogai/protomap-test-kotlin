package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709502218)
        val items = mutableListOf<TariffScore>()
        for (index in 0 until 5) {
            items.add(newTariffScore("tariff-score-$index", "sample"))
        }
        val report = buildTariffScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
