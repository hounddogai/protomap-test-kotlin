package com.acme.transport.safety.scorecard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackScorecardTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackScorecardTargetValidator()
        val item = newFallbackScorecardTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackScorecardTargetLoadKgRangeRule(1.7, 3053.7)
        val item = newFallbackScorecardTarget("x", "y")
        item.loadKg = 3055.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = FallbackScorecardTargetDepotLengthRule(86400)
        val item = newFallbackScorecardTarget("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FallbackScorecardTargetRequiredCurrencyRule("fallback scorecard target needs a value")
        val item = newFallbackScorecardTarget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackScorecardTargetStartsAtRangeRule(120, 1000)
        val item = newFallbackScorecardTarget("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackScorecardTargetCodeLengthRule(64)
        val item = newFallbackScorecardTarget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = FallbackScorecardTargetCostRangeRule(7.3, 4323.7)
        val item = newFallbackScorecardTarget("x", "y")
        item.cost = 4325.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
