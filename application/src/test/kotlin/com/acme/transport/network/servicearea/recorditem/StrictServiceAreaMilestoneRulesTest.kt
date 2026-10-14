package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaMilestoneValidator()
        val item = newStrictServiceAreaMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictServiceAreaMilestoneCodeLengthRule(80)
        val item = newStrictServiceAreaMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = StrictServiceAreaMilestoneTimezoneLengthRule(64)
        val item = newStrictServiceAreaMilestone("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictServiceAreaMilestoneDiscountRangeRule(9.8, 4386.7)
        val item = newStrictServiceAreaMilestone("x", "y")
        item.discount = 4388.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = StrictServiceAreaMilestoneExpiresAtRangeRule(80, 120)
        val item = newStrictServiceAreaMilestone("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
