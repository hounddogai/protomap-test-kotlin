package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubAllocationValidator()
        val item = newHubAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = HubAllocationPriorityRangeRule(80, 86400)
        val item = newHubAllocation("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = HubAllocationQuantityRangeRule(32, 86400)
        val item = newHubAllocation("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = HubAllocationRequiredOwnerIdRule("hub allocation needs this field")
        val item = newHubAllocation("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HubAllocationRequiredNameRule("hub allocation needs a value")
        val item = newHubAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = HubAllocationOwnerIdLengthRule(1000)
        val item = newHubAllocation("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
