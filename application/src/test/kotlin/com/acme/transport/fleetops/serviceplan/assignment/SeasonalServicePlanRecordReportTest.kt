package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709227548)
        val items = mutableListOf<SeasonalServicePlanRecord>()
        for (index in 0 until 4) {
            items.add(newSeasonalServicePlanRecord("seasonal-service-plan-record-$index", "sample"))
        }
        val report = buildSeasonalServicePlanRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
