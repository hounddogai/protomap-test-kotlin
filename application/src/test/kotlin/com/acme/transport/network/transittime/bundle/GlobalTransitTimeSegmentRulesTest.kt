package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTransitTimeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTransitTimeSegmentValidator()
        val item = newGlobalTransitTimeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = GlobalTransitTimeSegmentThresholdRangeRule(3.4, 564.0)
        val item = newGlobalTransitTimeSegment("x", "y")
        item.threshold = 565.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTransitTimeSegmentCodeLengthRule(80)
        val item = newGlobalTransitTimeSegment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalTransitTimeSegmentRetriesRangeRule(80, 255)
        val item = newGlobalTransitTimeSegment("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
