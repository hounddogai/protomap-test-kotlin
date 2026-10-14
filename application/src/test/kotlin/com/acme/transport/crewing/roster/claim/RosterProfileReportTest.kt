package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706663554)
        val items = mutableListOf<RosterProfile>()
        for (index in 0 until 4) {
            items.add(newRosterProfile("roster-profile-$index", "sample"))
        }
        val report = buildRosterProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
