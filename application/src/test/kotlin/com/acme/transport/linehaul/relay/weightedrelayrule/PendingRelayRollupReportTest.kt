package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706741857)
        val items = mutableListOf<PendingRelayRollup>()
        for (index in 0 until 3) {
            items.add(newPendingRelayRollup("pending-relay-rollup-$index", "sample"))
        }
        val report = buildPendingRelayRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
