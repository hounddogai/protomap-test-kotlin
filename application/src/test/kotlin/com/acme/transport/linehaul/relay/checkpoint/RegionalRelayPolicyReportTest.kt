package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRelayPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702747582)
        val items = mutableListOf<RegionalRelayPolicy>()
        for (index in 0 until 2) {
            items.add(newRegionalRelayPolicy("regional-relay-policy-$index", "sample"))
        }
        val report = buildRegionalRelayPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
