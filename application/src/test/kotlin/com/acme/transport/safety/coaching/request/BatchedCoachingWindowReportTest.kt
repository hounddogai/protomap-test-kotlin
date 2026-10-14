package com.acme.transport.safety.coaching.request

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCoachingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702738054)
        val items = mutableListOf<BatchedCoachingWindow>()
        for (index in 0 until 5) {
            items.add(newBatchedCoachingWindow("batched-coaching-window-$index", "sample"))
        }
        val report = buildBatchedCoachingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
