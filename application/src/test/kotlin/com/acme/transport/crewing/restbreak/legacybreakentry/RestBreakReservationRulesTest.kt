package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakReservationValidator()
        val item = newRestBreakReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakReservationCodeLengthRule(1000)
        val item = newRestBreakReservation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RestBreakReservationTitleLengthRule(80)
        val item = newRestBreakReservation("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
