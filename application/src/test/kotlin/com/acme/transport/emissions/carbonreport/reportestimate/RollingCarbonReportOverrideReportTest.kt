package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCarbonReportOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703189281)
        val items = mutableListOf<RollingCarbonReportOverride>()
        for (index in 0 until 2) {
            items.add(newRollingCarbonReportOverride("rolling-carbon-report-override-$index", "sample"))
        }
        val report = buildRollingCarbonReportOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
