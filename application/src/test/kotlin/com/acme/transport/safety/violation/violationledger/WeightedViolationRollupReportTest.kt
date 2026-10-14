package com.acme.transport.safety.violation.violationledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708560524)
        val items = mutableListOf<WeightedViolationRollup>()
        for (index in 0 until 4) {
            items.add(newWeightedViolationRollup("weighted-violation-rollup-$index", "sample"))
        }
        val report = buildWeightedViolationRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
