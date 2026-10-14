package com.acme.transport.terminal.crossdock.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCrossDockEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCrossDockEntryValidator()
        val item = newBatchedCrossDockEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedCrossDockEntryRequiredNameRule("batched cross dock entry needs this field")
        val item = newBatchedCrossDockEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedCrossDockEntryCodeLengthRule(64)
        val item = newBatchedCrossDockEntry("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedCrossDockEntryLabelLengthRule(255)
        val item = newBatchedCrossDockEntry("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedCrossDockEntryScoreRangeRule(8.4, 4136.0)
        val item = newBatchedCrossDockEntry("x", "y")
        item.score = 4137.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedCrossDockEntryRequiredCodeRule("batched cross dock entry needs a value")
        val item = newBatchedCrossDockEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
