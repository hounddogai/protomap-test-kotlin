package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708178646)
        val items = mutableListOf<DefaultParcelVariance>()
        for (index in 0 until 6) {
            items.add(newDefaultParcelVariance("default-parcel-variance-$index", "sample"))
        }
        val report = buildDefaultParcelVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
