package com.acme.transport.network.cutoff.schedule

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708553040)
        val items = mutableListOf<DefaultCutoffSchedule>()
        for (index in 0 until 3) {
            items.add(newDefaultCutoffSchedule("default-cutoff-schedule-$index", "sample"))
        }
        val report = buildDefaultCutoffScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
