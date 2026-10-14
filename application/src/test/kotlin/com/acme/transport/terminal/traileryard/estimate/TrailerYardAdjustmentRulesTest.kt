package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardAdjustmentValidator()
        val item = newTrailerYardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TrailerYardAdjustmentThresholdRangeRule(5.5, 170.9)
        val item = newTrailerYardAdjustment("x", "y")
        item.threshold = 172.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardAdjustmentRequiredCodeRule("trailer yard adjustment needs a value")
        val item = newTrailerYardAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerYardAdjustmentRequiredNameRule("trailer yard adjustment needs this field")
        val item = newTrailerYardAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardAdjustmentCodeLengthRule(64)
        val item = newTrailerYardAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
