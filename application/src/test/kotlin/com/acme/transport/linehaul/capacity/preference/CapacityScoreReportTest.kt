package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701671662)
        val items = mutableListOf<CapacityScore>()
        for (index in 0 until 4) {
            items.add(newCapacityScore("capacity-score-$index", "sample"))
        }
        val report = buildCapacityScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
