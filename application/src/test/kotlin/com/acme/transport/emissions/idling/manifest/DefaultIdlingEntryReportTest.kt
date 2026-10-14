package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707740660)
        val items = mutableListOf<DefaultIdlingEntry>()
        for (index in 0 until 4) {
            items.add(newDefaultIdlingEntry("default-idling-entry-$index", "sample"))
        }
        val report = buildDefaultIdlingEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
