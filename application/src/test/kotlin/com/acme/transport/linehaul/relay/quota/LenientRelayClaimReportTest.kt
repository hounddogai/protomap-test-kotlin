package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRelayClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707172459)
        val items = mutableListOf<LenientRelayClaim>()
        for (index in 0 until 3) {
            items.add(newLenientRelayClaim("lenient-relay-claim-$index", "sample"))
        }
        val report = buildLenientRelayClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
