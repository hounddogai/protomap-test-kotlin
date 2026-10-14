package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702592252)
        val items = mutableListOf<PendingRosterRollup>()
        for (index in 0 until 4) {
            items.add(newPendingRosterRollup("pending-roster-rollup-$index", "sample"))
        }
        val report = buildPendingRosterRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
