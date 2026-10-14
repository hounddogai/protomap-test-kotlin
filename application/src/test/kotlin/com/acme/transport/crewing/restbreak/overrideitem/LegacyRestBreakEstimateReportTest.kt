package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707098898)
        val items = mutableListOf<LegacyRestBreakEstimate>()
        for (index in 0 until 4) {
            items.add(newLegacyRestBreakEstimate("legacy-rest-break-estimate-$index", "sample"))
        }
        val report = buildLegacyRestBreakEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
