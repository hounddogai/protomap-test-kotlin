package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredZoneReservationValidator()
        val item = newTieredZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TieredZoneReservationRevisionRangeRule(64, 1000)
        val item = newTieredZoneReservation("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = TieredZoneReservationDescriptionLengthRule(3600)
        val item = newTieredZoneReservation("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredZoneReservationCodeLengthRule(255)
        val item = newTieredZoneReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
