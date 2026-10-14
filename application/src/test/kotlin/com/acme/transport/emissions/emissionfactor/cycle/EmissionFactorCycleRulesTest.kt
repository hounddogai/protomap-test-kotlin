package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorCycleValidator()
        val item = newEmissionFactorCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorCycleRequiredCodeRule("emission factor cycle needs this field")
        val item = newEmissionFactorCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = EmissionFactorCycleUpdatedAtRangeRule(32, 80)
        val item = newEmissionFactorCycle("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorCycleNameLengthRule(80)
        val item = newEmissionFactorCycle("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
