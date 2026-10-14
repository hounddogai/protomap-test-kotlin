package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDutyPeriodAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709860473)
        val items = mutableListOf<CappedDutyPeriodAdjustment>()
        for (index in 0 until 2) {
            items.add(newCappedDutyPeriodAdjustment("capped-duty-period-adjustment-$index", "sample"))
        }
        val report = buildCappedDutyPeriodAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
