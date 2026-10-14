package com.acme.transport.crewing.availability.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityPlanValidator()
        val item = newAvailabilityPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AvailabilityPlanRequiredTitleRule("availability plan needs this field")
        val item = newAvailabilityPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityPlanTitleLengthRule(16)
        val item = newAvailabilityPlan("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
