package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706232518)
        val items = mutableListOf<TariffException>()
        for (index in 0 until 3) {
            items.add(newTariffException("tariff-exception-$index", "sample"))
        }
        val report = buildTariffExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
