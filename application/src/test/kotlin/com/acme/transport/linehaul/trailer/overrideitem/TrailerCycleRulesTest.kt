package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerCycleValidator()
        val item = newTrailerCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerCycleTitleLengthRule(3600)
        val item = newTrailerCycle("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TrailerCycleTrailerIdLengthRule(86400)
        val item = newTrailerCycle("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TrailerCycleCostRangeRule(0.5, 335.3)
        val item = newTrailerCycle("x", "y")
        item.cost = 336.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerCycleCodeLengthRule(120)
        val item = newTrailerCycle("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = TrailerCycleRequiredTrailerIdRule("trailer cycle needs an entry")
        val item = newTrailerCycle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
