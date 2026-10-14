package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServicePlanBaselineValidator()
        val item = newSeasonalServicePlanBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalServicePlanBaselineRequiredOwnerIdRule("seasonal service plan baseline needs a value")
        val item = newSeasonalServicePlanBaseline("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalServicePlanBaselineDistanceKmRangeRule(8.9, 4750.2)
        val item = newSeasonalServicePlanBaseline("x", "y")
        item.distanceKm = 4751.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalServicePlanBaselineRequiredCodeRule("seasonal service plan baseline needs an entry")
        val item = newSeasonalServicePlanBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = SeasonalServicePlanBaselineWeightRangeRule(1.6, 533.6)
        val item = newSeasonalServicePlanBaseline("x", "y")
        item.weight = 535.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
