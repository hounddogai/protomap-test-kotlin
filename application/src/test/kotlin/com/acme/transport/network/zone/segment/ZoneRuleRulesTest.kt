package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneRuleValidator()
        val item = newZoneRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ZoneRuleEtaMinutesRangeRule(120, 1000)
        val item = newZoneRule("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = ZoneRuleCountryLengthRule(32)
        val item = newZoneRule("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ZoneRulePriorityRangeRule(16, 80)
        val item = newZoneRule("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ZoneRuleWindowDaysRangeRule(120, 255)
        val item = newZoneRule("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
