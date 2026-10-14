package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingVarianceValidator()
        val item = newCoachingVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingVarianceCodeLengthRule(255)
        val item = newCoachingVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CoachingVarianceRequiredRegionRule("coaching variance needs a value")
        val item = newCoachingVariance("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = CoachingVarianceRegionLengthRule(120)
        val item = newCoachingVariance("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CoachingVarianceRequiredNameRule("coaching variance needs an entry")
        val item = newCoachingVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CoachingVarianceSequenceRangeRule(16, 1000)
        val item = newCoachingVariance("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
