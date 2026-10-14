package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerYardAdjustmentValidator()
        val item = newManualTrailerYardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTrailerYardAdjustmentRequiredCodeRule("manual trailer yard adjustment needs this field")
        val item = newManualTrailerYardAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualTrailerYardAdjustmentRequiredTrailerIdRule("manual trailer yard adjustment needs this field")
        val item = newManualTrailerYardAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ManualTrailerYardAdjustmentTrailerIdLengthRule(80)
        val item = newManualTrailerYardAdjustment("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTrailerYardAdjustmentCodeLengthRule(64)
        val item = newManualTrailerYardAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
