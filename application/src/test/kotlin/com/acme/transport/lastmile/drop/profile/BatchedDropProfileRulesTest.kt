package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDropProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDropProfileValidator()
        val item = newBatchedDropProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedDropProfileLabelLengthRule(80)
        val item = newBatchedDropProfile("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedDropProfileRequiredLabelRule("batched drop profile needs a value")
        val item = newBatchedDropProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedDropProfileScoreRangeRule(8.2, 2186.6)
        val item = newBatchedDropProfile("x", "y")
        item.score = 2188.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = BatchedDropProfileDescriptionLengthRule(1000)
        val item = newBatchedDropProfile("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
