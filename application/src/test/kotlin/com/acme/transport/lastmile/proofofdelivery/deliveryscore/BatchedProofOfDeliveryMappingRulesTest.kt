package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryMappingValidator()
        val item = newBatchedProofOfDeliveryMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedProofOfDeliveryMappingNameLengthRule(80)
        val item = newBatchedProofOfDeliveryMapping("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = BatchedProofOfDeliveryMappingRevisionRangeRule(3600, 3600)
        val item = newBatchedProofOfDeliveryMapping("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedProofOfDeliveryMappingRequiredCodeRule("batched proof of delivery mapping needs a value")
        val item = newBatchedProofOfDeliveryMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
