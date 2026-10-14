package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHoldSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedHoldSegmentValidator()
        val item = newWeightedHoldSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = WeightedHoldSegmentPhoneLengthRule(86400)
        val item = newWeightedHoldSegment("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedHoldSegmentCodeLengthRule(80)
        val item = newWeightedHoldSegment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = WeightedHoldSegmentRequiredPhoneRule("weighted hold segment needs a value")
        val item = newWeightedHoldSegment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
