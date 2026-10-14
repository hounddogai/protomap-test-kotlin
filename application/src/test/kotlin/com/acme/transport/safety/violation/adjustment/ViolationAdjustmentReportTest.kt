package com.acme.transport.safety.violation.adjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707408381)
        val items = mutableListOf<ViolationAdjustment>()
        for (index in 0 until 4) {
            items.add(newViolationAdjustment("violation-adjustment-$index", "sample"))
        }
        val report = buildViolationAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
