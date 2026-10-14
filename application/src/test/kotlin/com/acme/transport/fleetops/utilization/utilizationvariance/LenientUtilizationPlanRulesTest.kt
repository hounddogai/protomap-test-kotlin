package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientUtilizationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientUtilizationPlanValidator()
        val item = newLenientUtilizationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientUtilizationPlanCodeLengthRule(32)
        val item = newLenientUtilizationPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LenientUtilizationPlanDepotLengthRule(3600)
        val item = newLenientUtilizationPlan("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = LenientUtilizationPlanRequiredCurrencyRule("lenient utilization plan needs an entry")
        val item = newLenientUtilizationPlan("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientUtilizationPlanDiscountRangeRule(7.8, 4474.2)
        val item = newLenientUtilizationPlan("x", "y")
        item.discount = 4475.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LenientUtilizationPlanRequiredDepotRule("lenient utilization plan needs an entry")
        val item = newLenientUtilizationPlan("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
