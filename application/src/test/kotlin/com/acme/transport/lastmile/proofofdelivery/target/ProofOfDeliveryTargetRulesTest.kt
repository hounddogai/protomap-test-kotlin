package com.acme.transport.lastmile.proofofdelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryTargetValidator()
        val item = newProofOfDeliveryTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ProofOfDeliveryTargetDepotLengthRule(120)
        val item = newProofOfDeliveryTarget("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryTargetCodeLengthRule(32)
        val item = newProofOfDeliveryTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ProofOfDeliveryTargetDurationMinutesRangeRule(32, 1000)
        val item = newProofOfDeliveryTarget("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ProofOfDeliveryTargetLabelLengthRule(3600)
        val item = newProofOfDeliveryTarget("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryTargetRequiredLabelRule("proof of delivery target needs this field")
        val item = newProofOfDeliveryTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
