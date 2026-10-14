package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelBurnAssignmentValidator()
        val item = newGlobalFuelBurnAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelBurnAssignmentCodeLengthRule(3600)
        val item = newGlobalFuelBurnAssignment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
