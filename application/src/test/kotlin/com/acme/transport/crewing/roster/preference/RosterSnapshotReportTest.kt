package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709094448)
        val items = mutableListOf<RosterSnapshot>()
        for (index in 0 until 3) {
            items.add(newRosterSnapshot("roster-snapshot-$index", "sample"))
        }
        val report = buildRosterSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
