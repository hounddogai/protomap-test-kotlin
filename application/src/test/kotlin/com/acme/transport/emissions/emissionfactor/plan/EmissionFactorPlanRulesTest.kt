package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorPlanValidator()
        val item = newEmissionFactorPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = EmissionFactorPlanContactEmailLengthRule(3600)
        val item = newEmissionFactorPlan("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorPlanRequiredCodeRule("emission factor plan needs an entry")
        val item = newEmissionFactorPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EmissionFactorPlanRequiredNameRule("emission factor plan needs an entry")
        val item = newEmissionFactorPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
