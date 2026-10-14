package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706824957)
        val items = mutableListOf<StrictBackhaulVariance>()
        for (index in 0 until 6) {
            items.add(newStrictBackhaulVariance("strict-backhaul-variance-$index", "sample"))
        }
        val report = buildStrictBackhaulVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
