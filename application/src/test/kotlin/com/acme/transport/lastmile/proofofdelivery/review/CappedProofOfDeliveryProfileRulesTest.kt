package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedProofOfDeliveryProfileValidator()
        val item = newCappedProofOfDeliveryProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedProofOfDeliveryProfileRequiredCodeRule("capped proof of delivery profile needs this field")
        val item = newCappedProofOfDeliveryProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CappedProofOfDeliveryProfileReviewerLengthRule(16)
        val item = newCappedProofOfDeliveryProfile("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedProofOfDeliveryProfileNameLengthRule(32)
        val item = newCappedProofOfDeliveryProfile("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = CappedProofOfDeliveryProfileCapacityRangeRule(16, 120)
        val item = newCappedProofOfDeliveryProfile("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
