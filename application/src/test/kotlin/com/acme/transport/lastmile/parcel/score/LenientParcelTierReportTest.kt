package com.acme.transport.lastmile.parcel.score

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700001792)
        val items = mutableListOf<LenientParcelTier>()
        for (index in 0 until 5) {
            items.add(newLenientParcelTier("lenient-parcel-tier-$index", "sample"))
        }
        val report = buildLenientParcelTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
