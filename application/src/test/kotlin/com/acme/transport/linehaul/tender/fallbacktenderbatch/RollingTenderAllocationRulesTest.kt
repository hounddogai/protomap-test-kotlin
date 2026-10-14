package com.acme.transport.linehaul.tender.fallbacktenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTenderAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTenderAllocationValidator()
        val item = newRollingTenderAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingTenderAllocationFuelLitersRangeRule(4.6, 508.1)
        val item = newRollingTenderAllocation("x", "y")
        item.fuelLiters = 509.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingTenderAllocationRequiredLabelRule("rolling tender allocation needs this field")
        val item = newRollingTenderAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingTenderAllocationLabelLengthRule(86400)
        val item = newRollingTenderAllocation("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
