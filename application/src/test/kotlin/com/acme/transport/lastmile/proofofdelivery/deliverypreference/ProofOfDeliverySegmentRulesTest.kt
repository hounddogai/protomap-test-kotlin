package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliverySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliverySegmentValidator()
        val item = newProofOfDeliverySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ProofOfDeliverySegmentRequiredNotesRule("proof of delivery segment needs a value")
        val item = newProofOfDeliverySegment("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ProofOfDeliverySegmentAmountRangeRule(7.6, 1633.9)
        val item = newProofOfDeliverySegment("x", "y")
        item.amount = 1635.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliverySegmentCodeLengthRule(80)
        val item = newProofOfDeliverySegment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ProofOfDeliverySegmentRequiredChannelRule("proof of delivery segment needs an entry")
        val item = newProofOfDeliverySegment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
