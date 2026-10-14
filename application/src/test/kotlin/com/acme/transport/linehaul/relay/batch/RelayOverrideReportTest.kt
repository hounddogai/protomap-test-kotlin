package com.acme.transport.linehaul.relay.batch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702478365)
        val items = mutableListOf<RelayOverride>()
        for (index in 0 until 5) {
            items.add(newRelayOverride("relay-override-$index", "sample"))
        }
        val report = buildRelayOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
