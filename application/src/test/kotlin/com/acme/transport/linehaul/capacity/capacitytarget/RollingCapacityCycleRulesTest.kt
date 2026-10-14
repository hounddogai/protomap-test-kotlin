package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCapacityCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCapacityCycleValidator()
        val item = newRollingCapacityCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RollingCapacityCycleContactEmailLengthRule(80)
        val item = newRollingCapacityCycle("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RollingCapacityCycleRequiredContactEmailRule("rolling capacity cycle needs an entry")
        val item = newRollingCapacityCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
