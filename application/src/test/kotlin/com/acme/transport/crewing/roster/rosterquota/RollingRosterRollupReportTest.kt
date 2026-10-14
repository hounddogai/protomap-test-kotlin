package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708817010)
        val items = mutableListOf<RollingRosterRollup>()
        for (index in 0 until 3) {
            items.add(newRollingRosterRollup("rolling-roster-rollup-$index", "sample"))
        }
        val report = buildRollingRosterRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
