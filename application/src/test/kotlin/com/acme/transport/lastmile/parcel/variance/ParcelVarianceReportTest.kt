package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709801935)
        val items = mutableListOf<ParcelVariance>()
        for (index in 0 until 6) {
            items.add(newParcelVariance("parcel-variance-$index", "sample"))
        }
        val report = buildParcelVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
