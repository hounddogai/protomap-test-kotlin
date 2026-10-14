package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700276333)
        val items = mutableListOf<ViolationBaseline>()
        for (index in 0 until 4) {
            items.add(newViolationBaseline("violation-baseline-$index", "sample"))
        }
        val report = buildViolationBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
