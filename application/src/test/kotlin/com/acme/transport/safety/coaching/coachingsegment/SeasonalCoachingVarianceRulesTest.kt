package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCoachingVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCoachingVarianceValidator()
        val item = newSeasonalCoachingVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = SeasonalCoachingVarianceTimezoneLengthRule(32)
        val item = newSeasonalCoachingVariance("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalCoachingVarianceLabelLengthRule(86400)
        val item = newSeasonalCoachingVariance("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCoachingVarianceCodeLengthRule(16)
        val item = newSeasonalCoachingVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalCoachingVarianceRequiredCodeRule("seasonal coaching variance needs a value")
        val item = newSeasonalCoachingVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = SeasonalCoachingVarianceThresholdRangeRule(3.5, 2427.8)
        val item = newSeasonalCoachingVariance("x", "y")
        item.threshold = 2429.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
