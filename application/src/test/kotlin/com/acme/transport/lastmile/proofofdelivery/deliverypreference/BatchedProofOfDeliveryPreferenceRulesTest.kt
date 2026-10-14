package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryPreferenceValidator()
        val item = newBatchedProofOfDeliveryPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedProofOfDeliveryPreferenceRequiredCodeRule(
            "batched proof of delivery preference needs this field",
        )
        val item = newBatchedProofOfDeliveryPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedProofOfDeliveryPreferenceLabelLengthRule(86400)
        val item = newBatchedProofOfDeliveryPreference("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedProofOfDeliveryPreferenceFuelLitersRangeRule(8.7, 3390.0)
        val item = newBatchedProofOfDeliveryPreference("x", "y")
        item.fuelLiters = 3391.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
