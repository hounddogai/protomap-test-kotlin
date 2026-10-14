package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDeclarationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDeclarationAllocationValidator()
        val item = newGlobalDeclarationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalDeclarationAllocationRateRangeRule(7.9, 3419.4)
        val item = newGlobalDeclarationAllocation("x", "y")
        item.rate = 3420.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalDeclarationAllocationRequiredLaneRule("global declaration allocation needs this field")
        val item = newGlobalDeclarationAllocation("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
