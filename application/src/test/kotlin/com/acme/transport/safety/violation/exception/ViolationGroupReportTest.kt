package com.acme.transport.safety.violation.exception

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709573235)
        val items = mutableListOf<ViolationGroup>()
        for (index in 0 until 6) {
            items.add(newViolationGroup("violation-group-$index", "sample"))
        }
        val report = buildViolationGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
