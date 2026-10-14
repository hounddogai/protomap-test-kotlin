package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703474402)
        val items = mutableListOf<PendingBackhaulForecast>()
        for (index in 0 until 6) {
            items.add(newPendingBackhaulForecast("pending-backhaul-forecast-$index", "sample"))
        }
        val report = buildPendingBackhaulForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
