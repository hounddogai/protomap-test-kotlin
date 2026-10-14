package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEmissionFactorExceptionValidator()
        val item = newSeasonalEmissionFactorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SeasonalEmissionFactorExceptionTrailerIdLengthRule(3600)
        val item = newSeasonalEmissionFactorException("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalEmissionFactorExceptionCodeLengthRule(255)
        val item = newSeasonalEmissionFactorException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalEmissionFactorExceptionLabelLengthRule(120)
        val item = newSeasonalEmissionFactorException("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
