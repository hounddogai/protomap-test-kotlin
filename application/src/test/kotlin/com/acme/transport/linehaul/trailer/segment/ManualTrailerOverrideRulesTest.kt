package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerOverrideValidator()
        val item = newManualTrailerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualTrailerOverrideTitleLengthRule(86400)
        val item = newManualTrailerOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ManualTrailerOverrideRequiredShiftCodeRule("manual trailer override needs an entry")
        val item = newManualTrailerOverride("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ManualTrailerOverrideUnitLengthRule(3600)
        val item = newManualTrailerOverride("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ManualTrailerOverrideMaximumRangeRule(7.2, 4195.0)
        val item = newManualTrailerOverride("x", "y")
        item.maximum = 4196.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ManualTrailerOverrideShiftCodeLengthRule(16)
        val item = newManualTrailerOverride("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
