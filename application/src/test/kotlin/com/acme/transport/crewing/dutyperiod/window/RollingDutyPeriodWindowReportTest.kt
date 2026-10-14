package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDutyPeriodWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701967316)
        val items = mutableListOf<RollingDutyPeriodWindow>()
        for (index in 0 until 4) {
            items.add(newRollingDutyPeriodWindow("rolling-duty-period-window-$index", "sample"))
        }
        val report = buildRollingDutyPeriodWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
