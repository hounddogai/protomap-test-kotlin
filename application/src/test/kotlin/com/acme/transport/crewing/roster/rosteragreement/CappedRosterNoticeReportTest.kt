package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRosterNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708244023)
        val items = mutableListOf<CappedRosterNotice>()
        for (index in 0 until 4) {
            items.add(newCappedRosterNotice("capped-roster-notice-$index", "sample"))
        }
        val report = buildCappedRosterNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
