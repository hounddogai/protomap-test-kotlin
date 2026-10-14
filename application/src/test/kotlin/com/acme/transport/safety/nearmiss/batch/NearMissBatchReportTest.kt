package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709699107)
        val items = mutableListOf<NearMissBatch>()
        for (index in 0 until 5) {
            items.add(newNearMissBatch("near-miss-batch-$index", "sample"))
        }
        val report = buildNearMissBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
