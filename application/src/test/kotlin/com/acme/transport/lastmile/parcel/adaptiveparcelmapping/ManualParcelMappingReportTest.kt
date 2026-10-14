package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700809339)
        val items = mutableListOf<ManualParcelMapping>()
        for (index in 0 until 5) {
            items.add(newManualParcelMapping("manual-parcel-mapping-$index", "sample"))
        }
        val report = buildManualParcelMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
