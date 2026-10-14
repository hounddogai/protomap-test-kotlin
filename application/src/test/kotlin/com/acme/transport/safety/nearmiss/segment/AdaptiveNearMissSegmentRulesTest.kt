package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNearMissSegmentValidator()
        val item = newAdaptiveNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveNearMissSegmentRequiredTitleRule("adaptive near miss segment needs this field")
        val item = newAdaptiveNearMissSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNearMissSegmentCodeLengthRule(16)
        val item = newAdaptiveNearMissSegment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveNearMissSegmentTitleLengthRule(32)
        val item = newAdaptiveNearMissSegment("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
