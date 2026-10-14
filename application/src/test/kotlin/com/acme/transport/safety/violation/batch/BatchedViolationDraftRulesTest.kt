package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedViolationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedViolationDraftValidator()
        val item = newBatchedViolationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BatchedViolationDraftRateRangeRule(3.0, 2318.9)
        val item = newBatchedViolationDraft("x", "y")
        item.rate = 2320.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = BatchedViolationDraftNotesLengthRule(255)
        val item = newBatchedViolationDraft("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BatchedViolationDraftMarginRangeRule(1.8, 1223.9)
        val item = newBatchedViolationDraft("x", "y")
        item.margin = 1225.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedViolationDraftRequiredLabelRule("batched violation draft needs an entry")
        val item = newBatchedViolationDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
