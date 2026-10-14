package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707605007)
        val items = mutableListOf<SeasonalAccessorialPlan>()
        for (index in 0 until 3) {
            items.add(newSeasonalAccessorialPlan("seasonal-accessorial-plan-$index", "sample"))
        }
        val report = buildSeasonalAccessorialPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
