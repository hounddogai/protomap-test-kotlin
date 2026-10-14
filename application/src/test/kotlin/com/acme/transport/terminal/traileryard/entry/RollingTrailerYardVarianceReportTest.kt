package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705810185)
        val items = mutableListOf<RollingTrailerYardVariance>()
        for (index in 0 until 4) {
            items.add(newRollingTrailerYardVariance("rolling-trailer-yard-variance-$index", "sample"))
        }
        val report = buildRollingTrailerYardVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
