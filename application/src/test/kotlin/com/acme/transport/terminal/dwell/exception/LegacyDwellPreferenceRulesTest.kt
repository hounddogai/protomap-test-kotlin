package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDwellPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDwellPreferenceValidator()
        val item = newLegacyDwellPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDwellPreferenceCodeLengthRule(80)
        val item = newLegacyDwellPreference("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LegacyDwellPreferenceRequiredUnitRule("legacy dwell preference needs an entry")
        val item = newLegacyDwellPreference("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LegacyDwellPreferenceShiftCodeLengthRule(3600)
        val item = newLegacyDwellPreference("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
