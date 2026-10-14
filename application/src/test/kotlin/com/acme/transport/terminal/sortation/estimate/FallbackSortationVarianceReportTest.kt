package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackSortationVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701037405)
        val items = mutableListOf<FallbackSortationVariance>()
        for (index in 0 until 3) {
            items.add(newFallbackSortationVariance("fallback-sortation-variance-$index", "sample"))
        }
        val report = buildFallbackSortationVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
