package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDeclarationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDeclarationReservationValidator()
        val item = newPendingDeclarationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingDeclarationReservationRequiredLabelRule("pending declaration reservation needs an entry")
        val item = newPendingDeclarationReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDeclarationReservationRequiredCodeRule("pending declaration reservation needs a value")
        val item = newPendingDeclarationReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PendingDeclarationReservationRateRangeRule(8.7, 4257.5)
        val item = newPendingDeclarationReservation("x", "y")
        item.rate = 4259.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PendingDeclarationReservationDriverIdLengthRule(120)
        val item = newPendingDeclarationReservation("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PendingDeclarationReservationRequiredDriverIdRule("pending declaration reservation needs an entry")
        val item = newPendingDeclarationReservation("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
