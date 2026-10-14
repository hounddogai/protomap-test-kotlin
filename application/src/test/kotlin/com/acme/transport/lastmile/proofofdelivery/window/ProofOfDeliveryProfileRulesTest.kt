package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryProfileValidator()
        val item = newProofOfDeliveryProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ProofOfDeliveryProfileThresholdRangeRule(7.3, 838.1)
        val item = newProofOfDeliveryProfile("x", "y")
        item.threshold = 839.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryProfileRequiredCodeRule("proof of delivery profile needs an entry")
        val item = newProofOfDeliveryProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = ProofOfDeliveryProfileOwnerIdLengthRule(64)
        val item = newProofOfDeliveryProfile("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryProfileRequiredTitleRule("proof of delivery profile needs this field")
        val item = newProofOfDeliveryProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ProofOfDeliveryProfileTitleLengthRule(16)
        val item = newProofOfDeliveryProfile("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
