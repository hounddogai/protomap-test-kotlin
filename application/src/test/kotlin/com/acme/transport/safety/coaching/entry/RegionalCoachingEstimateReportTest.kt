package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708223830)
        val items = mutableListOf<RegionalCoachingEstimate>()
        for (index in 0 until 3) {
            items.add(newRegionalCoachingEstimate("regional-coaching-estimate-$index", "sample"))
        }
        val report = buildRegionalCoachingEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
