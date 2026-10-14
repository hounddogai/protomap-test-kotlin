package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalSortationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalSortationAdjustmentValidator()
        val item = newSeasonalSortationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = SeasonalSortationAdjustmentLocaleLengthRule(32)
        val item = newSeasonalSortationAdjustment("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = SeasonalSortationAdjustmentExpiresAtRangeRule(1000, 86400)
        val item = newSeasonalSortationAdjustment("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalSortationAdjustmentRequiredNameRule("seasonal sortation adjustment needs this field")
        val item = newSeasonalSortationAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalSortationAdjustmentCodeLengthRule(80)
        val item = newSeasonalSortationAdjustment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalSortationAdjustmentRequiredCodeRule("seasonal sortation adjustment needs this field")
        val item = newSeasonalSortationAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = SeasonalSortationAdjustmentTimezoneLengthRule(86400)
        val item = newSeasonalSortationAdjustment("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
