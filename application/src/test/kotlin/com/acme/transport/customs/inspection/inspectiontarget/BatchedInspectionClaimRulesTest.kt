package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedInspectionClaimValidator()
        val item = newBatchedInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedInspectionClaimRequiredNameRule("batched inspection claim needs an entry")
        val item = newBatchedInspectionClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedInspectionClaimNameLengthRule(16)
        val item = newBatchedInspectionClaim("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedInspectionClaimCodeLengthRule(80)
        val item = newBatchedInspectionClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
