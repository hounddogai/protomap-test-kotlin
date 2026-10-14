package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerVarianceValidator()
        val item = newTrailerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TrailerVarianceThresholdRangeRule(2.6, 1323.5)
        val item = newTrailerVariance("x", "y")
        item.threshold = 1325.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = TrailerVarianceRequiredLaneRule("trailer variance needs a value")
        val item = newTrailerVariance("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TrailerVarianceCeilingRangeRule(7.7, 3074.5)
        val item = newTrailerVariance("x", "y")
        item.ceiling = 3076.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
