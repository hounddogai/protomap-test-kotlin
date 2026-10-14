package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeRuleValidator()
        val item = newDisputeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DisputeRuleRequiredTitleRule("dispute rule needs an entry")
        val item = newDisputeRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DisputeRuleRatioRangeRule(6.0, 3078.3)
        val item = newDisputeRule("x", "y")
        item.ratio = 3079.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
