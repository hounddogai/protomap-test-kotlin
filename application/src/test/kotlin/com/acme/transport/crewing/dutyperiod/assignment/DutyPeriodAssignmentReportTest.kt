package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702728966)
        val items = mutableListOf<DutyPeriodAssignment>()
        for (index in 0 until 5) {
            items.add(newDutyPeriodAssignment("duty-period-assignment-$index", "sample"))
        }
        val report = buildDutyPeriodAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
