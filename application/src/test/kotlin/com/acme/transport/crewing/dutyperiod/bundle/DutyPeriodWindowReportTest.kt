package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701856966)
        val items = mutableListOf<DutyPeriodWindow>()
        for (index in 0 until 5) {
            items.add(newDutyPeriodWindow("duty-period-window-$index", "sample"))
        }
        val report = buildDutyPeriodWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
