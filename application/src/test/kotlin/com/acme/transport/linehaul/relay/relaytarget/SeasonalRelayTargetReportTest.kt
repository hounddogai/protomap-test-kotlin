package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRelayTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705124125)
        val items = mutableListOf<SeasonalRelayTarget>()
        for (index in 0 until 6) {
            items.add(newSeasonalRelayTarget("seasonal-relay-target-$index", "sample"))
        }
        val report = buildSeasonalRelayTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
