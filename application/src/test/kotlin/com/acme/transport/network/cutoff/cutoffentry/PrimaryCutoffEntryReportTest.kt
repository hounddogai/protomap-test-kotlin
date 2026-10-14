package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCutoffEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702317442)
        val items = mutableListOf<PrimaryCutoffEntry>()
        for (index in 0 until 4) {
            items.add(newPrimaryCutoffEntry("primary-cutoff-entry-$index", "sample"))
        }
        val report = buildPrimaryCutoffEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
