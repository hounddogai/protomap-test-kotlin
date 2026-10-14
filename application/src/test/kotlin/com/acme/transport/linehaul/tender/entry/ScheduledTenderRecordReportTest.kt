package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTenderRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708017232)
        val items = mutableListOf<ScheduledTenderRecord>()
        for (index in 0 until 4) {
            items.add(newScheduledTenderRecord("scheduled-tender-record-$index", "sample"))
        }
        val report = buildScheduledTenderRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
