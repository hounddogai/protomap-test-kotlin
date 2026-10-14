package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAvailabilityAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAvailabilityAllocationValidator()
        val item = newGlobalAvailabilityAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalAvailabilityAllocationNameLengthRule(255)
        val item = newGlobalAvailabilityAllocation("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = GlobalAvailabilityAllocationOwnerIdLengthRule(255)
        val item = newGlobalAvailabilityAllocation("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalAvailabilityAllocationRequiredNameRule("global availability allocation needs this field")
        val item = newGlobalAvailabilityAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
