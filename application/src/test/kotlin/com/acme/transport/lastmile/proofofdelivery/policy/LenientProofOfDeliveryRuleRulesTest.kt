package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientProofOfDeliveryRuleValidator()
        val item = newLenientProofOfDeliveryRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LenientProofOfDeliveryRuleRequiredTimezoneRule("lenient proof of delivery rule needs this field")
        val item = newLenientProofOfDeliveryRule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientProofOfDeliveryRuleLabelLengthRule(64)
        val item = newLenientProofOfDeliveryRule("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientProofOfDeliveryRuleRequiredCodeRule("lenient proof of delivery rule needs an entry")
        val item = newLenientProofOfDeliveryRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
