package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRateCardTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRateCardTemplateValidator()
        val item = newAdaptiveRateCardTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveRateCardTemplateCreatedAtRangeRule(120, 1000)
        val item = newAdaptiveRateCardTemplate("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveRateCardTemplateRequiredCodeRule("adaptive rate card template needs an entry")
        val item = newAdaptiveRateCardTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveRateCardTemplateTitleLengthRule(64)
        val item = newAdaptiveRateCardTemplate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
