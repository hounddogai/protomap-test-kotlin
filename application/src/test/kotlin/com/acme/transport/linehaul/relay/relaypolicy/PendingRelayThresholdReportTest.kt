package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702967110)
        val items = mutableListOf<PendingRelayThreshold>()
        for (index in 0 until 3) {
            items.add(newPendingRelayThreshold("pending-relay-threshold-$index", "sample"))
        }
        val report = buildPendingRelayThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
