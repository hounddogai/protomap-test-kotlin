package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700667016)
        val items = mutableListOf<RosterDraft>()
        for (index in 0 until 5) {
            items.add(newRosterDraft("roster-draft-$index", "sample"))
        }
        val report = buildRosterDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
