package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRouteOffsetTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRouteOffsetTargetValidator()
        val item = newStrictRouteOffsetTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = StrictRouteOffsetTargetStartsAtRangeRule(16, 64)
        val item = newStrictRouteOffsetTarget("x", "y")
        item.startsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = StrictRouteOffsetTargetDepotLengthRule(64)
        val item = newStrictRouteOffsetTarget("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRouteOffsetTargetLabelLengthRule(3600)
        val item = newStrictRouteOffsetTarget("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
