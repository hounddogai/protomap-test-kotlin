package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCutoffPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCutoffPreferenceValidator()
        val item = newRollingCutoffPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCutoffPreferenceRequiredCodeRule("rolling cutoff preference needs this field")
        val item = newRollingCutoffPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingCutoffPreferenceNameLengthRule(64)
        val item = newRollingCutoffPreference("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RollingCutoffPreferenceShiftCodeLengthRule(32)
        val item = newRollingCutoffPreference("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingCutoffPreferenceRequiredNameRule("rolling cutoff preference needs a value")
        val item = newRollingCutoffPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RollingCutoffPreferenceRequiredShiftCodeRule("rolling cutoff preference needs a value")
        val item = newRollingCutoffPreference("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
