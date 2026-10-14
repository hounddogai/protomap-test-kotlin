package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706220354)
        val items = mutableListOf<ServicePlanPreference>()
        for (index in 0 until 5) {
            items.add(newServicePlanPreference("service-plan-preference-$index", "sample"))
        }
        val report = buildServicePlanPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
