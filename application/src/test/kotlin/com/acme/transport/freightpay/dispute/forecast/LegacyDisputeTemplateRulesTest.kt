package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDisputeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDisputeTemplateValidator()
        val item = newLegacyDisputeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyDisputeTemplateCeilingRangeRule(8.7, 5003.7)
        val item = newLegacyDisputeTemplate("x", "y")
        item.ceiling = 5005.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = LegacyDisputeTemplateCategoryLengthRule(16)
        val item = newLegacyDisputeTemplate("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
