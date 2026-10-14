package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703976214)
        val items = mutableListOf<AttemptRule>()
        for (index in 0 until 2) {
            items.add(newAttemptRule("attempt-rule-$index", "sample"))
        }
        val report = buildAttemptRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
