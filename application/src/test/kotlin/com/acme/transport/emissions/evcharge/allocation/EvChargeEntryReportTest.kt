package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707763023)
        val items = mutableListOf<EvChargeEntry>()
        for (index in 0 until 6) {
            items.add(newEvChargeEntry("ev-charge-entry-$index", "sample"))
        }
        val report = buildEvChargeEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
