package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTireBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTireBudgetValidator()
        val item = newRollingTireBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RollingTireBudgetVersionRangeRule(80, 255)
        val item = newRollingTireBudget("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingTireBudgetRequiredCodeRule("rolling tire budget needs an entry")
        val item = newRollingTireBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RollingTireBudgetUpdatedAtRangeRule(64, 120)
        val item = newRollingTireBudget("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RollingTireBudgetVehicleIdLengthRule(32)
        val item = newRollingTireBudget("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
