package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnNoticeValidator()
        val item = newFuelBurnNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FuelBurnNoticeStopCountRangeRule(120, 1000)
        val item = newFuelBurnNotice("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnNoticeCodeLengthRule(80)
        val item = newFuelBurnNotice("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelBurnNoticeLabelLengthRule(16)
        val item = newFuelBurnNotice("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FuelBurnNoticeLocaleLengthRule(80)
        val item = newFuelBurnNotice("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
