package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706365077)
        val items = mutableListOf<ManualInspectionForecast>()
        for (index in 0 until 3) {
            items.add(newManualInspectionForecast("manual-inspection-forecast-$index", "sample"))
        }
        val report = buildManualInspectionForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
