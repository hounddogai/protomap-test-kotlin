package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706925022)
        val items = mutableListOf<ScheduledRosterProfile>()
        for (index in 0 until 2) {
            items.add(newScheduledRosterProfile("scheduled-roster-profile-$index", "sample"))
        }
        val report = buildScheduledRosterProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
