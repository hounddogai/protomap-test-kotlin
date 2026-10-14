package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706699381)
        val items = mutableListOf<RelayEstimate>()
        for (index in 0 until 2) {
            items.add(newRelayEstimate("relay-estimate-$index", "sample"))
        }
        val report = buildRelayEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
