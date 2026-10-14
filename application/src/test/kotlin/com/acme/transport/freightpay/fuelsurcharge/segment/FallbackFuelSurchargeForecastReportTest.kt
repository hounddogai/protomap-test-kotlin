package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelSurchargeForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704680516)
        val items = mutableListOf<FallbackFuelSurchargeForecast>()
        for (index in 0 until 5) {
            items.add(newFallbackFuelSurchargeForecast("fallback-fuel-surcharge-forecast-$index", "sample"))
        }
        val report = buildFallbackFuelSurchargeForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
