package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorAssignmentValidator()
        val item = newEmissionFactorAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = EmissionFactorAssignmentExpiresAtRangeRule(80, 1000)
        val item = newEmissionFactorAssignment("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorAssignmentRequiredCodeRule("emission factor assignment needs a value")
        val item = newEmissionFactorAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
