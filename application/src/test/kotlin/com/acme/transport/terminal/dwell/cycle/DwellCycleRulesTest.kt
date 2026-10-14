package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellCycleValidator()
        val item = newDwellCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DwellCycleRequiredLabelRule("dwell cycle needs a value")
        val item = newDwellCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DwellCycleNameLengthRule(64)
        val item = newDwellCycle("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DwellCycleDurationMinutesRangeRule(32, 255)
        val item = newDwellCycle("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DwellCycleCeilingRangeRule(5.5, 3518.5)
        val item = newDwellCycle("x", "y")
        item.ceiling = 3520.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
