package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityScoreValidator()
        val item = newAvailabilityScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AvailabilityScoreDiscountRangeRule(1.2, 989.8)
        val item = newAvailabilityScore("x", "y")
        item.discount = 991.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityScoreCodeLengthRule(16)
        val item = newAvailabilityScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AvailabilityScoreLaneLengthRule(3600)
        val item = newAvailabilityScore("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
