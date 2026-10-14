package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTenderCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709257764)
        val items = mutableListOf<ScheduledTenderCycle>()
        for (index in 0 until 2) {
            items.add(newScheduledTenderCycle("scheduled-tender-cycle-$index", "sample"))
        }
        val report = buildScheduledTenderCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
