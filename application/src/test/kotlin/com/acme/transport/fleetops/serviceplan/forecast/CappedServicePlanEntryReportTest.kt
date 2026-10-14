package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServicePlanEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700588757)
        val items = mutableListOf<CappedServicePlanEntry>()
        for (index in 0 until 2) {
            items.add(newCappedServicePlanEntry("capped-service-plan-entry-$index", "sample"))
        }
        val report = buildCappedServicePlanEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
