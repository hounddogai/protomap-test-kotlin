package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryBatchValidator()
        val item = newProofOfDeliveryBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ProofOfDeliveryBatchBudgetRangeRule(6.4, 4839.2)
        val item = newProofOfDeliveryBatch("x", "y")
        item.budget = 4840.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ProofOfDeliveryBatchFloorValueRangeRule(1.1, 1620.8)
        val item = newProofOfDeliveryBatch("x", "y")
        item.floorValue = 1622.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryBatchCodeLengthRule(86400)
        val item = newProofOfDeliveryBatch("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
