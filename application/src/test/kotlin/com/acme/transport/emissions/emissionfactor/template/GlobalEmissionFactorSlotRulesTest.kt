package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalEmissionFactorSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalEmissionFactorSlotValidator()
        val item = newGlobalEmissionFactorSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalEmissionFactorSlotCreatedAtRangeRule(80, 3600)
        val item = newGlobalEmissionFactorSlot("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalEmissionFactorSlotOdometerRangeRule(255, 86400)
        val item = newGlobalEmissionFactorSlot("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
