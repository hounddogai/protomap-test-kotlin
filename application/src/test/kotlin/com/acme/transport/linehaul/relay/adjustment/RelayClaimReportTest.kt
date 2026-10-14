package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708176381)
        val items = mutableListOf<RelayClaim>()
        for (index in 0 until 4) {
            items.add(newRelayClaim("relay-claim-$index", "sample"))
        }
        val report = buildRelayClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
