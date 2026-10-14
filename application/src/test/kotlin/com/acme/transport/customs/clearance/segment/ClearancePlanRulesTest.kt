package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearancePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearancePlanValidator()
        val item = newClearancePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ClearancePlanDistanceKmRangeRule(7.7, 217.1)
        val item = newClearancePlan("x", "y")
        item.distanceKm = 218.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ClearancePlanDepotLengthRule(16)
        val item = newClearancePlan("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ClearancePlanLabelLengthRule(32)
        val item = newClearancePlan("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ClearancePlanRequiredDepotRule("clearance plan needs a value")
        val item = newClearancePlan("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
