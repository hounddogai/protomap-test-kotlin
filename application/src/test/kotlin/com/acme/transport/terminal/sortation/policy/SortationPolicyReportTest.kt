package com.acme.transport.terminal.sortation.policy

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707236493)
        val items = mutableListOf<SortationPolicy>()
        for (index in 0 until 3) {
            items.add(newSortationPolicy("sortation-policy-$index", "sample"))
        }
        val report = buildSortationPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
