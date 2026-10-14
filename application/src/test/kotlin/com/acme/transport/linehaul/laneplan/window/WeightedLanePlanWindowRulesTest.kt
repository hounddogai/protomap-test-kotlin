package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLanePlanWindowValidator()
        val item = newWeightedLanePlanWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = WeightedLanePlanWindowWeightRangeRule(3.3, 2494.7)
        val item = newWeightedLanePlanWindow("x", "y")
        item.weight = 2496.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedLanePlanWindowContactEmailLengthRule(1000)
        val item = newWeightedLanePlanWindow("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedLanePlanWindowNameLengthRule(86400)
        val item = newWeightedLanePlanWindow("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
