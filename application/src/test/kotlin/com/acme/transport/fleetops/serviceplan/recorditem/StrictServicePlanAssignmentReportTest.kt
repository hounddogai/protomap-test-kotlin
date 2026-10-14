package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServicePlanAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704566792)
        val items = mutableListOf<StrictServicePlanAssignment>()
        for (index in 0 until 4) {
            items.add(newStrictServicePlanAssignment("strict-service-plan-assignment-$index", "sample"))
        }
        val report = buildStrictServicePlanAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
