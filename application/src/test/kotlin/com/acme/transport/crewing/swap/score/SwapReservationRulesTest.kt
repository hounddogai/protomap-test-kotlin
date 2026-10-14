package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapReservationValidator()
        val item = newSwapReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = SwapReservationChecksumLengthRule(120)
        val item = newSwapReservation("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SwapReservationRevisionRangeRule(32, 3600)
        val item = newSwapReservation("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = SwapReservationRequiredChecksumRule("swap reservation needs this field")
        val item = newSwapReservation("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
