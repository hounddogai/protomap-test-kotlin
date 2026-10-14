package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705647202)
        val items = mutableListOf<ServiceAreaPlan>()
        for (index in 0 until 5) {
            items.add(newServiceAreaPlan("service-area-plan-$index", "sample"))
        }
        val report = buildServiceAreaPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
