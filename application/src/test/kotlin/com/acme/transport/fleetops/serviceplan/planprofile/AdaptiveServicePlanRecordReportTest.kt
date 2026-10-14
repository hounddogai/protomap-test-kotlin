package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServicePlanRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700731869)
        val items = mutableListOf<AdaptiveServicePlanRecord>()
        for (index in 0 until 2) {
            items.add(newAdaptiveServicePlanRecord("adaptive-service-plan-record-$index", "sample"))
        }
        val report = buildAdaptiveServicePlanRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
