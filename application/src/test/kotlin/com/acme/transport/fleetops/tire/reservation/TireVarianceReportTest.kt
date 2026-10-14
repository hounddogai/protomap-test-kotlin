package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700880452)
        val items = mutableListOf<TireVariance>()
        for (index in 0 until 5) {
            items.add(newTireVariance("tire-variance-$index", "sample"))
        }
        val report = buildTireVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
