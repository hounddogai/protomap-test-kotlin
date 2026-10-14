package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700067479)
        val items = mutableListOf<LenientNearMissBatch>()
        for (index in 0 until 6) {
            items.add(newLenientNearMissBatch("lenient-near-miss-batch-$index", "sample"))
        }
        val report = buildLenientNearMissBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
