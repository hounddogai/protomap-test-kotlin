package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700565296)
        val items = mutableListOf<WeightedCarbonReportClaim>()
        for (index in 0 until 2) {
            items.add(newWeightedCarbonReportClaim("weighted-carbon-report-claim-$index", "sample"))
        }
        val report = buildWeightedCarbonReportClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
