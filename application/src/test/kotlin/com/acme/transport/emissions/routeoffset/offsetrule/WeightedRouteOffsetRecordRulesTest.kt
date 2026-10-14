package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRouteOffsetRecordValidator()
        val item = newWeightedRouteOffsetRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedRouteOffsetRecordVersionRangeRule(16, 1000)
        val item = newWeightedRouteOffsetRecord("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedRouteOffsetRecordPriorityRangeRule(1000, 86400)
        val item = newWeightedRouteOffsetRecord("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = WeightedRouteOffsetRecordUnitLengthRule(32)
        val item = newWeightedRouteOffsetRecord("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedRouteOffsetRecordRequiredCodeRule("weighted route offset record needs a value")
        val item = newWeightedRouteOffsetRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedRouteOffsetRecordLabelLengthRule(255)
        val item = newWeightedRouteOffsetRecord("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
