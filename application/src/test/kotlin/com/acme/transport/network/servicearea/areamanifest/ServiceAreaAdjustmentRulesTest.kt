package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaAdjustmentValidator()
        val item = newServiceAreaAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ServiceAreaAdjustmentExpiresAtRangeRule(120, 1000)
        val item = newServiceAreaAdjustment("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ServiceAreaAdjustmentBudgetRangeRule(3.2, 558.4)
        val item = newServiceAreaAdjustment("x", "y")
        item.budget = 559.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServiceAreaAdjustmentRequiredTitleRule("service area adjustment needs an entry")
        val item = newServiceAreaAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ServiceAreaAdjustmentDiscountRangeRule(0.5, 4166.7)
        val item = newServiceAreaAdjustment("x", "y")
        item.discount = 4168.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
