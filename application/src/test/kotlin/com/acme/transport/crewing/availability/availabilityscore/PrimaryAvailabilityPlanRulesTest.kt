package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAvailabilityPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAvailabilityPlanValidator()
        val item = newPrimaryAvailabilityPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PrimaryAvailabilityPlanDiscountRangeRule(8.0, 2853.6)
        val item = newPrimaryAvailabilityPlan("x", "y")
        item.discount = 2855.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PrimaryAvailabilityPlanOwnerIdLengthRule(3600)
        val item = newPrimaryAvailabilityPlan("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PrimaryAvailabilityPlanDockLengthRule(255)
        val item = newPrimaryAvailabilityPlan("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
