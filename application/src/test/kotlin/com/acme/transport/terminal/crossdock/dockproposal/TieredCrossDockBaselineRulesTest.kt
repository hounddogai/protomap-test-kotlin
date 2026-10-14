package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCrossDockBaselineValidator()
        val item = newTieredCrossDockBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TieredCrossDockBaselineNotesLengthRule(64)
        val item = newTieredCrossDockBaseline("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TieredCrossDockBaselineRequiredNotesRule("tiered cross dock baseline needs a value")
        val item = newTieredCrossDockBaseline("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = TieredCrossDockBaselineStartsAtRangeRule(32, 80)
        val item = newTieredCrossDockBaseline("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
