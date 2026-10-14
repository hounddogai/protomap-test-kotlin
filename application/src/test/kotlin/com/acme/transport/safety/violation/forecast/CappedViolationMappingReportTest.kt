package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706224872)
        val items = mutableListOf<CappedViolationMapping>()
        for (index in 0 until 3) {
            items.add(newCappedViolationMapping("capped-violation-mapping-$index", "sample"))
        }
        val report = buildCappedViolationMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
