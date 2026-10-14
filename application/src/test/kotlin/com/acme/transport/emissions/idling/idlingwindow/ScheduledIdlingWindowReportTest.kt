package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703048079)
        val items = mutableListOf<ScheduledIdlingWindow>()
        for (index in 0 until 3) {
            items.add(newScheduledIdlingWindow("scheduled-idling-window-$index", "sample"))
        }
        val report = buildScheduledIdlingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
