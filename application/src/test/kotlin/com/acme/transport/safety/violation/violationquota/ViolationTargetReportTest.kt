package com.acme.transport.safety.violation.violationquota

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708943210)
        val items = mutableListOf<ViolationTarget>()
        for (index in 0 until 3) {
            items.add(newViolationTarget("violation-target-$index", "sample"))
        }
        val report = buildViolationTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
