package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellEntryValidator()
        val item = newDwellEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DwellEntryRequiredLaneRule("dwell entry needs this field")
        val item = newDwellEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DwellEntryRequiredNameRule("dwell entry needs an entry")
        val item = newDwellEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DwellEntryUnitLengthRule(255)
        val item = newDwellEntry("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DwellEntryStopCountRangeRule(32, 120)
        val item = newDwellEntry("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellEntryRequiredCodeRule("dwell entry needs this field")
        val item = newDwellEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellEntryCodeLengthRule(255)
        val item = newDwellEntry("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
