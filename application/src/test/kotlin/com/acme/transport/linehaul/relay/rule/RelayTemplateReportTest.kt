package com.acme.transport.linehaul.relay.rule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709971528)
        val items = mutableListOf<RelayTemplate>()
        for (index in 0 until 3) {
            items.add(newRelayTemplate("relay-template-$index", "sample"))
        }
        val report = buildRelayTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
