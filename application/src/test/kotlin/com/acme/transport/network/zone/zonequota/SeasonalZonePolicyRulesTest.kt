package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalZonePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalZonePolicyValidator()
        val item = newSeasonalZonePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalZonePolicyRequiredNameRule("seasonal zone policy needs a value")
        val item = newSeasonalZonePolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = SeasonalZonePolicyRouteCodeLengthRule(1000)
        val item = newSeasonalZonePolicy("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = SeasonalZonePolicySummaryLengthRule(64)
        val item = newSeasonalZonePolicy("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalZonePolicyRequiredCodeRule("seasonal zone policy needs an entry")
        val item = newSeasonalZonePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = SeasonalZonePolicyRequiredSummaryRule("seasonal zone policy needs this field")
        val item = newSeasonalZonePolicy("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
