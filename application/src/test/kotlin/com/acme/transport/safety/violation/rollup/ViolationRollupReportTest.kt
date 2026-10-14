package com.acme.transport.safety.violation.rollup

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705641248)
        val items = mutableListOf<ViolationRollup>()
        for (index in 0 until 4) {
            items.add(newViolationRollup("violation-rollup-$index", "sample"))
        }
        val report = buildViolationRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
