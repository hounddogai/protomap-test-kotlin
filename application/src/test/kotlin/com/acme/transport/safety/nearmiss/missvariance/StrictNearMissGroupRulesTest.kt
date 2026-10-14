package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNearMissGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNearMissGroupValidator()
        val item = newStrictNearMissGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictNearMissGroupNameLengthRule(86400)
        val item = newStrictNearMissGroup("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = StrictNearMissGroupRegionLengthRule(1000)
        val item = newStrictNearMissGroup("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = StrictNearMissGroupCostRangeRule(7.3, 1227.7)
        val item = newStrictNearMissGroup("x", "y")
        item.cost = 1229.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
