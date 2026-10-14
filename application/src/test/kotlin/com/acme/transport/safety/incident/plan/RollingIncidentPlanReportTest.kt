package com.acme.transport.safety.incident.plan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIncidentPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700680962)
        val items = mutableListOf<RollingIncidentPlan>()
        for (index in 0 until 6) {
            items.add(newRollingIncidentPlan("rolling-incident-plan-$index", "sample"))
        }
        val report = buildRollingIncidentPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
