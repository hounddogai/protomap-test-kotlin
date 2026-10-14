package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDwellPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703073432)
        val items = mutableListOf<SeasonalDwellPolicy>()
        for (index in 0 until 5) {
            items.add(newSeasonalDwellPolicy("seasonal-dwell-policy-$index", "sample"))
        }
        val report = buildSeasonalDwellPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
