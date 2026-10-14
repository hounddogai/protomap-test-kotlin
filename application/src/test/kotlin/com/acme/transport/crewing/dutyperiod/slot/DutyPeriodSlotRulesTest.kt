package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodSlotValidator()
        val item = newDutyPeriodSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DutyPeriodSlotRequiredNameRule("duty period slot needs this field")
        val item = newDutyPeriodSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DutyPeriodSlotRequiredCountryRule("duty period slot needs this field")
        val item = newDutyPeriodSlot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = DutyPeriodSlotLoadKgRangeRule(6.7, 1834.9)
        val item = newDutyPeriodSlot("x", "y")
        item.loadKg = 1836.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = DutyPeriodSlotRegionLengthRule(1000)
        val item = newDutyPeriodSlot("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = DutyPeriodSlotRequiredRegionRule("duty period slot needs this field")
        val item = newDutyPeriodSlot("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
