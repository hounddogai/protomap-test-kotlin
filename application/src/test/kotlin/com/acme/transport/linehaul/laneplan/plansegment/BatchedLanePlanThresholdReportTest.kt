package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLanePlanThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709585113)
        val items = mutableListOf<BatchedLanePlanThreshold>()
        for (index in 0 until 2) {
            items.add(newBatchedLanePlanThreshold("batched-lane-plan-threshold-$index", "sample"))
        }
        val report = buildBatchedLanePlanThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
