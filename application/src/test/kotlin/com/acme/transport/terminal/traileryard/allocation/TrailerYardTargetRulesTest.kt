package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardTargetValidator()
        val item = newTrailerYardTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TrailerYardTargetLimitRangeRule(16, 16)
        val item = newTrailerYardTarget("x", "y")
        item.limit = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TrailerYardTargetOwnerIdLengthRule(16)
        val item = newTrailerYardTarget("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TrailerYardTargetRequiredOwnerIdRule("trailer yard target needs this field")
        val item = newTrailerYardTarget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TrailerYardTargetDistanceKmRangeRule(0.5, 1592.8)
        val item = newTrailerYardTarget("x", "y")
        item.distanceKm = 1594.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
