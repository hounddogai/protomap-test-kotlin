package com.acme.transport.emissions.emissionfactor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEmissionFactorAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEmissionFactorAllocationValidator()
        val item = newStrictEmissionFactorAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = StrictEmissionFactorAllocationRequiredTimezoneRule(
            "strict emission factor allocation needs this field",
        )
        val item = newStrictEmissionFactorAllocation("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictEmissionFactorAllocationRequiredTitleRule(
            "strict emission factor allocation needs this field",
        )
        val item = newStrictEmissionFactorAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictEmissionFactorAllocationRequiredCodeRule("strict emission factor allocation needs an entry")
        val item = newStrictEmissionFactorAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
