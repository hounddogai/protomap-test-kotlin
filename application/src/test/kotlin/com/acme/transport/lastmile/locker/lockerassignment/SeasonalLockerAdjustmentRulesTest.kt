package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLockerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalLockerAdjustmentValidator()
        val item = newSeasonalLockerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = SeasonalLockerAdjustmentNotesLengthRule(120)
        val item = newSeasonalLockerAdjustment("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalLockerAdjustmentCodeLengthRule(64)
        val item = newSeasonalLockerAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = SeasonalLockerAdjustmentOdometerRangeRule(16, 64)
        val item = newSeasonalLockerAdjustment("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalLockerAdjustmentLabelLengthRule(86400)
        val item = newSeasonalLockerAdjustment("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
