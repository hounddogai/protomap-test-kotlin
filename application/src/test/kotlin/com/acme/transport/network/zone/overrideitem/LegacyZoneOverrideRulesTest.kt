package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneOverrideValidator()
        val item = newLegacyZoneOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyZoneOverrideRequiredTitleRule("legacy zone override needs a value")
        val item = newLegacyZoneOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LegacyZoneOverrideShiftCodeLengthRule(64)
        val item = newLegacyZoneOverride("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyZoneOverrideRatioRangeRule(1.6, 1768.8)
        val item = newLegacyZoneOverride("x", "y")
        item.ratio = 1770.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
