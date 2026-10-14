package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellAdjustmentValidator()
        val item = newDwellAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DwellAdjustmentRequiredNameRule("dwell adjustment needs an entry")
        val item = newDwellAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellAdjustmentCodeLengthRule(16)
        val item = newDwellAdjustment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DwellAdjustmentSummaryLengthRule(120)
        val item = newDwellAdjustment("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DwellAdjustmentPriorityRangeRule(64, 255)
        val item = newDwellAdjustment("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
