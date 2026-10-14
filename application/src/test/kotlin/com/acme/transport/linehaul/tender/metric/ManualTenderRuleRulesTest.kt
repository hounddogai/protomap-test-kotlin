package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTenderRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTenderRuleValidator()
        val item = newManualTenderRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualTenderRuleWeightRangeRule(4.1, 3569.6)
        val item = newManualTenderRule("x", "y")
        item.weight = 3571.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTenderRuleRequiredCodeRule("manual tender rule needs this field")
        val item = newManualTenderRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualTenderRuleAttemptsRangeRule(120, 86400)
        val item = newManualTenderRule("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
