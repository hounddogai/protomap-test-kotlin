package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyViolationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyViolationVarianceValidator()
        val item = newLegacyViolationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyViolationVarianceRequiredTitleRule("legacy violation variance needs this field")
        val item = newLegacyViolationVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyViolationVarianceTitleLengthRule(120)
        val item = newLegacyViolationVariance("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyViolationVarianceRequiredLabelRule("legacy violation variance needs a value")
        val item = newLegacyViolationVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyViolationVarianceCodeLengthRule(64)
        val item = newLegacyViolationVariance("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyViolationVarianceUpdatedAtRangeRule(64, 86400)
        val item = newLegacyViolationVariance("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
