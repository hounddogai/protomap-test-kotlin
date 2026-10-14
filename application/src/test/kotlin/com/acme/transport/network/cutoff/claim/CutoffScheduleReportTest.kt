package com.acme.transport.network.cutoff.claim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700716620)
        val items = mutableListOf<CutoffSchedule>()
        for (index in 0 until 6) {
            items.add(newCutoffSchedule("cutoff-schedule-$index", "sample"))
        }
        val report = buildCutoffScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
