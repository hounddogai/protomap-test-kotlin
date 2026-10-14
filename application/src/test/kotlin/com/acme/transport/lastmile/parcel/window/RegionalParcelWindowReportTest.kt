package com.acme.transport.lastmile.parcel.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705960415)
        val items = mutableListOf<RegionalParcelWindow>()
        for (index in 0 until 4) {
            items.add(newRegionalParcelWindow("regional-parcel-window-$index", "sample"))
        }
        val report = buildRegionalParcelWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
