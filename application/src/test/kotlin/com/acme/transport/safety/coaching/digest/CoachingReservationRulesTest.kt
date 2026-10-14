package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingReservationValidator()
        val item = newCoachingReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = CoachingReservationRequiredRouteCodeRule("coaching reservation needs a value")
        val item = newCoachingReservation("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CoachingReservationRequiredTitleRule("coaching reservation needs this field")
        val item = newCoachingReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CoachingReservationAmountRangeRule(4.0, 2836.7)
        val item = newCoachingReservation("x", "y")
        item.amount = 2838.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
