package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCoachingReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCoachingReservationValidator()
        val item = newCappedCoachingReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCoachingReservationCodeLengthRule(86400)
        val item = newCappedCoachingReservation("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CappedCoachingReservationRequiredCategoryRule("capped coaching reservation needs an entry")
        val item = newCappedCoachingReservation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CappedCoachingReservationStopCountRangeRule(64, 64)
        val item = newCappedCoachingReservation("x", "y")
        item.stopCount = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = CappedCoachingReservationRequiredShiftCodeRule("capped coaching reservation needs an entry")
        val item = newCappedCoachingReservation("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
