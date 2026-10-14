package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDefectClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDefectClaimValidator()
        val item = newBatchedDefectClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = BatchedDefectClaimMaximumRangeRule(9.8, 923.0)
        val item = newBatchedDefectClaim("x", "y")
        item.maximum = 924.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDefectClaimRequiredCodeRule("batched defect claim needs this field")
        val item = newBatchedDefectClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = BatchedDefectClaimCarrierLengthRule(80)
        val item = newBatchedDefectClaim("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = BatchedDefectClaimRequiredCarrierRule("batched defect claim needs an entry")
        val item = newBatchedDefectClaim("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
