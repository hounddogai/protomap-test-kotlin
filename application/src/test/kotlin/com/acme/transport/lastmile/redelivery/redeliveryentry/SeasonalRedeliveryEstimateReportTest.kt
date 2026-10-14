package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704270538)
        val items = mutableListOf<SeasonalRedeliveryEstimate>()
        for (index in 0 until 6) {
            items.add(newSeasonalRedeliveryEstimate("seasonal-redelivery-estimate-$index", "sample"))
        }
        val report = buildSeasonalRedeliveryEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
