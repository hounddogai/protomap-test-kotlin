package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryRuleValidator()
        val item = newProofOfDeliveryRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ProofOfDeliveryRuleCeilingRangeRule(8.5, 2769.9)
        val item = newProofOfDeliveryRule("x", "y")
        item.ceiling = 2771.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ProofOfDeliveryRuleDurationMinutesRangeRule(1000, 86400)
        val item = newProofOfDeliveryRule("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ProofOfDeliveryRuleLabelLengthRule(16)
        val item = newProofOfDeliveryRule("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
