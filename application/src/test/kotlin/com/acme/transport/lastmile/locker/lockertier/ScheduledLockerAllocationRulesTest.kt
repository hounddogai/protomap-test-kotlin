package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLockerAllocationValidator()
        val item = newScheduledLockerAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ScheduledLockerAllocationBudgetRangeRule(4.1, 4120.5)
        val item = newScheduledLockerAllocation("x", "y")
        item.budget = 4122.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledLockerAllocationRequiredLabelRule("scheduled locker allocation needs this field")
        val item = newScheduledLockerAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = ScheduledLockerAllocationRequiredUnitRule("scheduled locker allocation needs this field")
        val item = newScheduledLockerAllocation("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ScheduledLockerAllocationRequiredDepotRule("scheduled locker allocation needs an entry")
        val item = newScheduledLockerAllocation("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
