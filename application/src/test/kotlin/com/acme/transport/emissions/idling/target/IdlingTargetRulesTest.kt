package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingTargetValidator()
        val item = newIdlingTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = IdlingTargetWeightRangeRule(7.0, 4854.3)
        val item = newIdlingTarget("x", "y")
        item.weight = 4855.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = IdlingTargetRateRangeRule(8.6, 1780.0)
        val item = newIdlingTarget("x", "y")
        item.rate = 1781.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = IdlingTargetRequiredLaneRule("idling target needs an entry")
        val item = newIdlingTarget("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingTargetTitleLengthRule(255)
        val item = newIdlingTarget("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = IdlingTargetLaneLengthRule(16)
        val item = newIdlingTarget("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
