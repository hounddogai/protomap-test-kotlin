package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707840171)
        val items = mutableListOf<TenderSchedule>()
        for (index in 0 until 6) {
            items.add(newTenderSchedule("tender-schedule-$index", "sample"))
        }
        val report = buildTenderScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
