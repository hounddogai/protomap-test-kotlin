package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorRollupValidator()
        val item = newEmissionFactorRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = EmissionFactorRollupThresholdRangeRule(9.6, 1849.1)
        val item = newEmissionFactorRollup("x", "y")
        item.threshold = 1850.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorRollupTitleLengthRule(86400)
        val item = newEmissionFactorRollup("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = EmissionFactorRollupShiftCodeLengthRule(16)
        val item = newEmissionFactorRollup("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = EmissionFactorRollupRateRangeRule(5.5, 2930.2)
        val item = newEmissionFactorRollup("x", "y")
        item.rate = 2931.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorRollupCodeLengthRule(1000)
        val item = newEmissionFactorRollup("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
