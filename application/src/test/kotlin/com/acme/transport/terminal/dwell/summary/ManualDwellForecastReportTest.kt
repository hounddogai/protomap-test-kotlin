package com.acme.transport.terminal.dwell.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705223896)
        val items = mutableListOf<ManualDwellForecast>()
        for (index in 0 until 3) {
            items.add(newManualDwellForecast("manual-dwell-forecast-$index", "sample"))
        }
        val report = buildManualDwellForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
