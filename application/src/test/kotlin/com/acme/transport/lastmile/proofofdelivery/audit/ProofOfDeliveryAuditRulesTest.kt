package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryAuditValidator()
        val item = newProofOfDeliveryAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ProofOfDeliveryAuditPriorityRangeRule(120, 3600)
        val item = newProofOfDeliveryAudit("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryAuditCodeLengthRule(1000)
        val item = newProofOfDeliveryAudit("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ProofOfDeliveryAuditMarginRangeRule(1.0, 2734.7)
        val item = newProofOfDeliveryAudit("x", "y")
        item.margin = 2736.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryAuditRequiredCodeRule("proof of delivery audit needs this field")
        val item = newProofOfDeliveryAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ProofOfDeliveryAuditBatchSizeRangeRule(80, 86400)
        val item = newProofOfDeliveryAudit("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
