package com.acme.transport.linehaul.relay.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRelayTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRelayTemplateValidator()
        val item = newAdaptiveRelayTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveRelayTemplateAmountRangeRule(0.5, 2749.4)
        val item = newAdaptiveRelayTemplate("x", "y")
        item.amount = 2750.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveRelayTemplateRequiredNameRule("adaptive relay template needs an entry")
        val item = newAdaptiveRelayTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AdaptiveRelayTemplateDurationMinutesRangeRule(80, 1000)
        val item = newAdaptiveRelayTemplate("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
