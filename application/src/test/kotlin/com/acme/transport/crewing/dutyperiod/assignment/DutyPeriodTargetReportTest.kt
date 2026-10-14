package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704988031)
        val items = mutableListOf<DutyPeriodTarget>()
        for (index in 0 until 4) {
            items.add(newDutyPeriodTarget("duty-period-target-$index", "sample"))
        }
        val report = buildDutyPeriodTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
