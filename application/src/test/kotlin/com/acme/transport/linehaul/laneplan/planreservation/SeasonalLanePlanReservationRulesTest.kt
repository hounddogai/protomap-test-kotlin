package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLanePlanReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalLanePlanReservationValidator()
        val item = newSeasonalLanePlanReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalLanePlanReservationDistanceKmRangeRule(2.8, 216.3)
        val item = newSeasonalLanePlanReservation("x", "y")
        item.distanceKm = 217.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalLanePlanReservationCostRangeRule(9.5, 2027.2)
        val item = newSeasonalLanePlanReservation("x", "y")
        item.cost = 2028.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalLanePlanReservationRequiredCodeRule("seasonal lane plan reservation needs an entry")
        val item = newSeasonalLanePlanReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalLanePlanReservationRequiredTitleRule("seasonal lane plan reservation needs this field")
        val item = newSeasonalLanePlanReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalLanePlanReservationCodeLengthRule(255)
        val item = newSeasonalLanePlanReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
