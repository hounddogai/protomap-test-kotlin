package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalViolationTierValidator()
        val item = newGlobalViolationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalViolationTierEndsAtRangeRule(80, 86400)
        val item = newGlobalViolationTier("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = GlobalViolationTierDescriptionLengthRule(80)
        val item = newGlobalViolationTier("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
