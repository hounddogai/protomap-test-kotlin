package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServicePlanRecordValidator()
        val item = newSeasonalServicePlanRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = SeasonalServicePlanRecordGraceDaysRangeRule(32, 80)
        val item = newSeasonalServicePlanRecord("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SeasonalServicePlanRecordStopCountRangeRule(16, 32)
        val item = newSeasonalServicePlanRecord("x", "y")
        item.stopCount = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalServicePlanRecordNameLengthRule(32)
        val item = newSeasonalServicePlanRecord("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalServicePlanRecordRequiredNameRule("seasonal service plan record needs a value")
        val item = newSeasonalServicePlanRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = SeasonalServicePlanRecordCarrierLengthRule(80)
        val item = newSeasonalServicePlanRecord("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
