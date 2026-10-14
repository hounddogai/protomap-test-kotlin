package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707500361)
        val items = mutableListOf<CarbonReportPreference>()
        for (index in 0 until 4) {
            items.add(newCarbonReportPreference("carbon-report-preference-$index", "sample"))
        }
        val report = buildCarbonReportPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
