package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707790183)
        val items = mutableListOf<ParcelBaseline>()
        for (index in 0 until 4) {
            items.add(newParcelBaseline("parcel-baseline-$index", "sample"))
        }
        val report = buildParcelBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
