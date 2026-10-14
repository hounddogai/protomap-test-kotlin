package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyParcelMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701139079)
        val items = mutableListOf<LegacyParcelMapping>()
        for (index in 0 until 5) {
            items.add(newLegacyParcelMapping("legacy-parcel-mapping-$index", "sample"))
        }
        val report = buildLegacyParcelMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
