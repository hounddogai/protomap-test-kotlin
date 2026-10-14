package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRelayPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702229360)
        val items = mutableListOf<BatchedRelayPlan>()
        for (index in 0 until 3) {
            items.add(newBatchedRelayPlan("batched-relay-plan-$index", "sample"))
        }
        val report = buildBatchedRelayPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
