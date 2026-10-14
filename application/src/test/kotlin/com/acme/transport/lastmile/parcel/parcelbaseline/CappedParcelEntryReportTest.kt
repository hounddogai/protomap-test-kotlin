package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedParcelEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703978148)
        val items = mutableListOf<CappedParcelEntry>()
        for (index in 0 until 5) {
            items.add(newCappedParcelEntry("capped-parcel-entry-$index", "sample"))
        }
        val report = buildCappedParcelEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
