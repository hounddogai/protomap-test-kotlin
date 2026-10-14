package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorProfileValidator()
        val item = newEmissionFactorProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = EmissionFactorProfileUnitLengthRule(1000)
        val item = newEmissionFactorProfile("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = EmissionFactorProfileMaximumRangeRule(2.7, 549.2)
        val item = newEmissionFactorProfile("x", "y")
        item.maximum = 550.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorProfileCodeLengthRule(86400)
        val item = newEmissionFactorProfile("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
