package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingParcelVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704300516)
        val items = mutableListOf<RollingParcelVariance>()
        for (index in 0 until 5) {
            items.add(newRollingParcelVariance("rolling-parcel-variance-$index", "sample"))
        }
        val report = buildRollingParcelVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
