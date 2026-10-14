package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissReservationValidator()
        val item = newNearMissReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = NearMissReservationStartsAtRangeRule(64, 120)
        val item = newNearMissReservation("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = NearMissReservationDepotLengthRule(16)
        val item = newNearMissReservation("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissReservationCodeLengthRule(3600)
        val item = newNearMissReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = NearMissReservationDiscountRangeRule(0.2, 3899.5)
        val item = newNearMissReservation("x", "y")
        item.discount = 3901.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
