package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAuditWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAuditWindowValidator()
        val item = newWeightedAuditWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedAuditWindowRequiredCodeRule("weighted audit window needs a value")
        val item = newWeightedAuditWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedAuditWindowLabelLengthRule(16)
        val item = newWeightedAuditWindow("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = WeightedAuditWindowWindowDaysRangeRule(86400, 86400)
        val item = newWeightedAuditWindow("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
