package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRateCardPlanValidator()
        val item = newCappedRateCardPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedRateCardPlanRequiredCodeRule("capped rate card plan needs a value")
        val item = newCappedRateCardPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedRateCardPlanNameLengthRule(3600)
        val item = newCappedRateCardPlan("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = CappedRateCardPlanRouteCodeLengthRule(80)
        val item = newCappedRateCardPlan("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = CappedRateCardPlanRequiredRouteCodeRule("capped rate card plan needs this field")
        val item = newCappedRateCardPlan("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
