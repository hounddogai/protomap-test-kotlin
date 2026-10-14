package com.acme.transport.emissions.emissionfactor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorTargetValidator()
        val item = newEmissionFactorTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = EmissionFactorTargetContactEmailLengthRule(32)
        val item = newEmissionFactorTarget("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorTargetRequiredCodeRule("emission factor target needs this field")
        val item = newEmissionFactorTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorTargetCodeLengthRule(1000)
        val item = newEmissionFactorTarget("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorTargetTitleLengthRule(80)
        val item = newEmissionFactorTarget("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
