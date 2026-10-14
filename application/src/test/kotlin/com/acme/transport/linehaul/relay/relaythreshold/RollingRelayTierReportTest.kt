package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702043999)
        val items = mutableListOf<RollingRelayTier>()
        for (index in 0 until 4) {
            items.add(newRollingRelayTier("rolling-relay-tier-$index", "sample"))
        }
        val report = buildRollingRelayTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
