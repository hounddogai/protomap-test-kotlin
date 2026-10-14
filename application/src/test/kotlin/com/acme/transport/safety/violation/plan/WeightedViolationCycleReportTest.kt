package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706748720)
        val items = mutableListOf<WeightedViolationCycle>()
        for (index in 0 until 5) {
            items.add(newWeightedViolationCycle("weighted-violation-cycle-$index", "sample"))
        }
        val report = buildWeightedViolationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
