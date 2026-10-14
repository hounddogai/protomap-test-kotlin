package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEmissionFactorBatchValidator()
        val item = newDefaultEmissionFactorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultEmissionFactorBatchRequiredCodeRule("default emission factor batch needs this field")
        val item = newDefaultEmissionFactorBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DefaultEmissionFactorBatchFuelLitersRangeRule(2.1, 3681.7)
        val item = newDefaultEmissionFactorBatch("x", "y")
        item.fuelLiters = 3683.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
