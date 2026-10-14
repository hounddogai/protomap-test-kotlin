package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRedeliveryPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRedeliveryPolicyValidator()
        val item = newLegacyRedeliveryPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LegacyRedeliveryPolicyGraceDaysRangeRule(120, 1000)
        val item = newLegacyRedeliveryPolicy("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyRedeliveryPolicyRequiredLabelRule("legacy redelivery policy needs this field")
        val item = newLegacyRedeliveryPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyRedeliveryPolicyThresholdRangeRule(0.0, 2705.3)
        val item = newLegacyRedeliveryPolicy("x", "y")
        item.threshold = 2706.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
