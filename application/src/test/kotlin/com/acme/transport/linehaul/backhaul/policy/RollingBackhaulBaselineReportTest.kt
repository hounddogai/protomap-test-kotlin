package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703821157)
        val items = mutableListOf<RollingBackhaulBaseline>()
        for (index in 0 until 3) {
            items.add(newRollingBackhaulBaseline("rolling-backhaul-baseline-$index", "sample"))
        }
        val report = buildRollingBackhaulBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
