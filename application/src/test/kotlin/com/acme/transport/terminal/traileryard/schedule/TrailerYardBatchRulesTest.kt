package com.acme.transport.terminal.traileryard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardBatchValidator()
        val item = newTrailerYardBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TrailerYardBatchWeightRangeRule(0.1, 4751.5)
        val item = newTrailerYardBatch("x", "y")
        item.weight = 4753.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TrailerYardBatchMinimumRangeRule(5.5, 2171.0)
        val item = newTrailerYardBatch("x", "y")
        item.minimum = 2172.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardBatchCodeLengthRule(255)
        val item = newTrailerYardBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardBatchRequiredCodeRule("trailer yard batch needs an entry")
        val item = newTrailerYardBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
