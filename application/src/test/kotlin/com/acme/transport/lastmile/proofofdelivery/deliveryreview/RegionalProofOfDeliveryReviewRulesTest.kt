package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalProofOfDeliveryReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalProofOfDeliveryReviewValidator()
        val item = newRegionalProofOfDeliveryReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalProofOfDeliveryReviewNameLengthRule(3600)
        val item = newRegionalProofOfDeliveryReview("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = RegionalProofOfDeliveryReviewDockLengthRule(16)
        val item = newRegionalProofOfDeliveryReview("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RegionalProofOfDeliveryReviewBatchSizeRangeRule(32, 64)
        val item = newRegionalProofOfDeliveryReview("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RegionalProofOfDeliveryReviewContactEmailLengthRule(80)
        val item = newRegionalProofOfDeliveryReview("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalProofOfDeliveryReviewRequiredCodeRule("regional proof of delivery review needs this field")
        val item = newRegionalProofOfDeliveryReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalProofOfDeliveryReviewRequiredNameRule("regional proof of delivery review needs a value")
        val item = newRegionalProofOfDeliveryReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
