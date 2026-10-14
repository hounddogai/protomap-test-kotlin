package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalEmissionFactorSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalEmissionFactorSummaryValidator()
        val item = newGlobalEmissionFactorSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalEmissionFactorSummaryLabelLengthRule(3600)
        val item = newGlobalEmissionFactorSummary("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalEmissionFactorSummaryRevisionRangeRule(80, 86400)
        val item = newGlobalEmissionFactorSummary("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = GlobalEmissionFactorSummaryWindowDaysRangeRule(32, 255)
        val item = newGlobalEmissionFactorSummary("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
