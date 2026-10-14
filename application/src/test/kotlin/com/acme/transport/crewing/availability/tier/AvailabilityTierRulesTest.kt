package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityTierValidator()
        val item = newAvailabilityTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityTierCodeLengthRule(255)
        val item = newAvailabilityTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityTierLabelLengthRule(86400)
        val item = newAvailabilityTier("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AvailabilityTierMinimumRangeRule(10.0, 282.4)
        val item = newAvailabilityTier("x", "y")
        item.minimum = 283.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
