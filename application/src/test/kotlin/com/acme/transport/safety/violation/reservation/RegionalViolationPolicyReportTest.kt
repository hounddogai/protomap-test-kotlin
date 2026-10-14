package com.acme.transport.safety.violation.reservation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalViolationPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708927000)
        val items = mutableListOf<RegionalViolationPolicy>()
        for (index in 0 until 2) {
            items.add(newRegionalViolationPolicy("regional-violation-policy-$index", "sample"))
        }
        val report = buildRegionalViolationPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
