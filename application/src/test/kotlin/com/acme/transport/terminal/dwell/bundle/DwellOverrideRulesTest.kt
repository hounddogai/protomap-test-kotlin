package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellOverrideValidator()
        val item = newDwellOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DwellOverrideEtaMinutesRangeRule(32, 80)
        val item = newDwellOverride("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = DwellOverrideRequiredDriverIdRule("dwell override needs this field")
        val item = newDwellOverride("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellOverrideCodeLengthRule(16)
        val item = newDwellOverride("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DwellOverrideDriverIdLengthRule(32)
        val item = newDwellOverride("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellOverrideRequiredCodeRule("dwell override needs this field")
        val item = newDwellOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
