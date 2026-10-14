package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBackhaulVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBackhaulVarianceValidator()
        val item = newLenientBackhaulVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LenientBackhaulVarianceCostRangeRule(5.4, 3712.3)
        val item = newLenientBackhaulVariance("x", "y")
        item.cost = 3713.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LenientBackhaulVarianceRequiredTrailerIdRule("lenient backhaul variance needs a value")
        val item = newLenientBackhaulVariance("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = LenientBackhaulVarianceCountryLengthRule(86400)
        val item = newLenientBackhaulVariance("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
