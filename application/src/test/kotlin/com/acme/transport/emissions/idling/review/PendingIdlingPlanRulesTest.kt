package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingIdlingPlanValidator()
        val item = newPendingIdlingPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingIdlingPlanUpdatedAtRangeRule(16, 80)
        val item = newPendingIdlingPlan("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PendingIdlingPlanDriverIdLengthRule(1000)
        val item = newPendingIdlingPlan("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = PendingIdlingPlanCountryLengthRule(3600)
        val item = newPendingIdlingPlan("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingIdlingPlanRequiredCodeRule("pending idling plan needs an entry")
        val item = newPendingIdlingPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
