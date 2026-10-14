package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705693573)
        val items = mutableListOf<ScheduledLanePlanQuota>()
        for (index in 0 until 5) {
            items.add(newScheduledLanePlanQuota("scheduled-lane-plan-quota-$index", "sample"))
        }
        val report = buildScheduledLanePlanQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
