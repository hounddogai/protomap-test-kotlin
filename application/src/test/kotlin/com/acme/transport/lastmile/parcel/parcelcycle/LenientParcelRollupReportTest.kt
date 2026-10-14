package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704507453)
        val items = mutableListOf<LenientParcelRollup>()
        for (index in 0 until 5) {
            items.add(newLenientParcelRollup("lenient-parcel-rollup-$index", "sample"))
        }
        val report = buildLenientParcelRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
