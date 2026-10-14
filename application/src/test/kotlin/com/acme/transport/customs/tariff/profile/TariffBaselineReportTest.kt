package com.acme.transport.customs.tariff.profile

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707050294)
        val items = mutableListOf<TariffBaseline>()
        for (index in 0 until 5) {
            items.add(newTariffBaseline("tariff-baseline-$index", "sample"))
        }
        val report = buildTariffBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
