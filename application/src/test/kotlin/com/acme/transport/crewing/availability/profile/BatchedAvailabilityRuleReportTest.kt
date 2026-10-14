package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAvailabilityRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705901493)
        val items = mutableListOf<BatchedAvailabilityRule>()
        for (index in 0 until 6) {
            items.add(newBatchedAvailabilityRule("batched-availability-rule-$index", "sample"))
        }
        val report = buildBatchedAvailabilityRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
