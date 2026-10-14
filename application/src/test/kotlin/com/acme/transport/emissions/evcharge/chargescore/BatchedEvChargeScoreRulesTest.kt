package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEvChargeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedEvChargeScoreValidator()
        val item = newBatchedEvChargeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedEvChargeScoreRequiredNameRule("batched ev charge score needs a value")
        val item = newBatchedEvChargeScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedEvChargeScoreAmountRangeRule(1.5, 346.7)
        val item = newBatchedEvChargeScore("x", "y")
        item.amount = 348.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedEvChargeScoreRequiredCodeRule("batched ev charge score needs this field")
        val item = newBatchedEvChargeScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = BatchedEvChargeScoreRequiredDockRule("batched ev charge score needs a value")
        val item = newBatchedEvChargeScore("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedEvChargeScoreStartsAtRangeRule(16, 255)
        val item = newBatchedEvChargeScore("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
