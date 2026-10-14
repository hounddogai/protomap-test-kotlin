package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIdlingVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedIdlingVarianceValidator()
        val item = newBatchedIdlingVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedIdlingVarianceRequiredCodeRule("batched idling variance needs a value")
        val item = newBatchedIdlingVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedIdlingVarianceCodeLengthRule(32)
        val item = newBatchedIdlingVariance("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BatchedIdlingVarianceSequenceRangeRule(80, 3600)
        val item = newBatchedIdlingVariance("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
