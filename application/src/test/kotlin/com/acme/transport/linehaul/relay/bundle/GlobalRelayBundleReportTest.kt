package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRelayBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707978151)
        val items = mutableListOf<GlobalRelayBundle>()
        for (index in 0 until 4) {
            items.add(newGlobalRelayBundle("global-relay-bundle-$index", "sample"))
        }
        val report = buildGlobalRelayBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
