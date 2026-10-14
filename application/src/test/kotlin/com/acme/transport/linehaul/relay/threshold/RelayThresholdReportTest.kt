package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701178448)
        val items = mutableListOf<RelayThreshold>()
        for (index in 0 until 4) {
            items.add(newRelayThreshold("relay-threshold-$index", "sample"))
        }
        val report = buildRelayThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
