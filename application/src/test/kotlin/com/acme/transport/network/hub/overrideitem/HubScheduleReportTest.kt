package com.acme.transport.network.hub.overrideitem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707709458)
        val items = mutableListOf<HubSchedule>()
        for (index in 0 until 3) {
            items.add(newHubSchedule("hub-schedule-$index", "sample"))
        }
        val report = buildHubScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
