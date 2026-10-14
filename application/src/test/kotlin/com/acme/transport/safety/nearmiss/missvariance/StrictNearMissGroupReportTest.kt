package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNearMissGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708482353)
        val items = mutableListOf<StrictNearMissGroup>()
        for (index in 0 until 2) {
            items.add(newStrictNearMissGroup("strict-near-miss-group-$index", "sample"))
        }
        val report = buildStrictNearMissGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
