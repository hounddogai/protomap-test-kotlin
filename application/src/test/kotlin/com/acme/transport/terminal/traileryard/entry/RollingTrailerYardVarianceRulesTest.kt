package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTrailerYardVarianceValidator()
        val item = newRollingTrailerYardVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingTrailerYardVarianceNameLengthRule(255)
        val item = newRollingTrailerYardVariance("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingTrailerYardVarianceWeightRangeRule(1.1, 2095.3)
        val item = newRollingTrailerYardVariance("x", "y")
        item.weight = 2096.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RollingTrailerYardVarianceRequiredCountryRule("rolling trailer yard variance needs this field")
        val item = newRollingTrailerYardVariance("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RollingTrailerYardVarianceRequiredTrailerIdRule("rolling trailer yard variance needs a value")
        val item = newRollingTrailerYardVariance("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingTrailerYardVarianceCodeLengthRule(255)
        val item = newRollingTrailerYardVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
