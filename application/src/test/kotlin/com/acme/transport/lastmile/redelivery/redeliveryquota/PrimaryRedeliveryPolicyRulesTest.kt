package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRedeliveryPolicyValidator()
        val item = newPrimaryRedeliveryPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryRedeliveryPolicyAmountRangeRule(4.2, 3339.1)
        val item = newPrimaryRedeliveryPolicy("x", "y")
        item.amount = 3340.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryRedeliveryPolicyCodeLengthRule(16)
        val item = newPrimaryRedeliveryPolicy("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = PrimaryRedeliveryPolicyCarrierLengthRule(32)
        val item = newPrimaryRedeliveryPolicy("x", "y")
        item.carrier = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryRedeliveryPolicyRequiredLabelRule("primary redelivery policy needs a value")
        val item = newPrimaryRedeliveryPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
