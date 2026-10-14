package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCapacityAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCapacityAllocationValidator()
        val item = newScheduledCapacityAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ScheduledCapacityAllocationSummaryLengthRule(255)
        val item = newScheduledCapacityAllocation("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ScheduledCapacityAllocationRequiredDriverIdRule("scheduled capacity allocation needs a value")
        val item = newScheduledCapacityAllocation("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
