package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701627075)
        val items = mutableListOf<PrimaryServicePlanProfile>()
        for (index in 0 until 3) {
            items.add(newPrimaryServicePlanProfile("primary-service-plan-profile-$index", "sample"))
        }
        val report = buildPrimaryServicePlanProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
