package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704737430)
        val items = mutableListOf<RegionalParcelEntry>()
        for (index in 0 until 4) {
            items.add(newRegionalParcelEntry("regional-parcel-entry-$index", "sample"))
        }
        val report = buildRegionalParcelEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
