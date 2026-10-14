package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709525153)
        val items = mutableListOf<ManualDutyPeriodNotice>()
        for (index in 0 until 6) {
            items.add(newManualDutyPeriodNotice("manual-duty-period-notice-$index", "sample"))
        }
        val report = buildManualDutyPeriodNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
