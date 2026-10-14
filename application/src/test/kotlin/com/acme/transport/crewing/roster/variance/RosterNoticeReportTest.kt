package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709504331)
        val items = mutableListOf<RosterNotice>()
        for (index in 0 until 5) {
            items.add(newRosterNotice("roster-notice-$index", "sample"))
        }
        val report = buildRosterNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
