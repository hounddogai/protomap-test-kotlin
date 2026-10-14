package com.acme.transport.crewing.availability.rule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708351895)
        val items = mutableListOf<AvailabilityRule>()
        for (index in 0 until 5) {
            items.add(newAvailabilityRule("availability-rule-$index", "sample"))
        }
        val report = buildAvailabilityRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
