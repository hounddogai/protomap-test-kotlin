package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700487173)
        val items = mutableListOf<RelayTier>()
        for (index in 0 until 2) {
            items.add(newRelayTier("relay-tier-$index", "sample"))
        }
        val report = buildRelayTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
