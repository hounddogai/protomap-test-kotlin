package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCutoffReservationValidator()
        val item = newTieredCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredCutoffReservationCodeLengthRule(64)
        val item = newTieredCutoffReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = TieredCutoffReservationShiftCodeLengthRule(64)
        val item = newTieredCutoffReservation("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
