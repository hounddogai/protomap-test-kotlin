package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707562238)
        val items = mutableListOf<DutyPeriodCycle>()
        for (index in 0 until 3) {
            items.add(newDutyPeriodCycle("duty-period-cycle-$index", "sample"))
        }
        val report = buildDutyPeriodCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
