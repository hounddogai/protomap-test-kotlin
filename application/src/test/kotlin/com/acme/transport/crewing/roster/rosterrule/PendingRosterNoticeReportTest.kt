package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708779429)
        val items = mutableListOf<PendingRosterNotice>()
        for (index in 0 until 2) {
            items.add(newPendingRosterNotice("pending-roster-notice-$index", "sample"))
        }
        val report = buildPendingRosterNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
