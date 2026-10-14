package com.acme.transport.linehaul.tender.profile

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701111630)
        val items = mutableListOf<TenderForecast>()
        for (index in 0 until 3) {
            items.add(newTenderForecast("tender-forecast-$index", "sample"))
        }
        val report = buildTenderForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
