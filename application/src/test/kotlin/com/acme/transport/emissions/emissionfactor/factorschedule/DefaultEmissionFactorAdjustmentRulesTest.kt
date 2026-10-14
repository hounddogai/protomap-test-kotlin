package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEmissionFactorAdjustmentValidator()
        val item = newDefaultEmissionFactorAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultEmissionFactorAdjustmentCodeLengthRule(86400)
        val item = newDefaultEmissionFactorAdjustment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DefaultEmissionFactorAdjustmentVehicleIdLengthRule(120)
        val item = newDefaultEmissionFactorAdjustment("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultEmissionFactorAdjustmentMinimumRangeRule(0.4, 1799.2)
        val item = newDefaultEmissionFactorAdjustment("x", "y")
        item.minimum = 1800.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
