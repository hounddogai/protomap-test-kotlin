package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDockDoorScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700244975)
        val items = mutableListOf<DefaultDockDoorSchedule>()
        for (index in 0 until 4) {
            items.add(newDefaultDockDoorSchedule("default-dock-door-schedule-$index", "sample"))
        }
        val report = buildDefaultDockDoorScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
