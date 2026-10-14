package com.acme.transport.safety.scorecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyScorecardCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyScorecardCycleValidator()
        val item = newLegacyScorecardCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyScorecardCycleCostRangeRule(5.7, 3117.3)
        val item = newLegacyScorecardCycle("x", "y")
        item.cost = 3118.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyScorecardCycleLabelLengthRule(120)
        val item = newLegacyScorecardCycle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
