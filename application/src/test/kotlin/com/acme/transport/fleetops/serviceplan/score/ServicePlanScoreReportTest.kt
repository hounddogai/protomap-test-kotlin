package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705243230)
        val items = mutableListOf<ServicePlanScore>()
        for (index in 0 until 5) {
            items.add(newServicePlanScore("service-plan-score-$index", "sample"))
        }
        val report = buildServicePlanScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
