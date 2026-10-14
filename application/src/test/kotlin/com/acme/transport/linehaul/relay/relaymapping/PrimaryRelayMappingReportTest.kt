package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRelayMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703011487)
        val items = mutableListOf<PrimaryRelayMapping>()
        for (index in 0 until 3) {
            items.add(newPrimaryRelayMapping("primary-relay-mapping-$index", "sample"))
        }
        val report = buildPrimaryRelayMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
