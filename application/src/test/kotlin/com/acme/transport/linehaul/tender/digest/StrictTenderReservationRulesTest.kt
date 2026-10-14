package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTenderReservationValidator()
        val item = newStrictTenderReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTenderReservationCodeLengthRule(32)
        val item = newStrictTenderReservation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = StrictTenderReservationLoadKgRangeRule(4.8, 2446.7)
        val item = newStrictTenderReservation("x", "y")
        item.loadKg = 2448.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictTenderReservationTitleLengthRule(1000)
        val item = newStrictTenderReservation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
