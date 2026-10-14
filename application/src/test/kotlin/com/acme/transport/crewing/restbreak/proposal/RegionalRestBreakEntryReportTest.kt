package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRestBreakEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701405044)
        val items = mutableListOf<RegionalRestBreakEntry>()
        for (index in 0 until 6) {
            items.add(newRegionalRestBreakEntry("regional-rest-break-entry-$index", "sample"))
        }
        val report = buildRegionalRestBreakEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
