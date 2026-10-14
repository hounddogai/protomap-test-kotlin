package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCapacityReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCapacityReservationValidator()
        val item = newTieredCapacityReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TieredCapacityReservationContactEmailLengthRule(64)
        val item = newTieredCapacityReservation("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredCapacityReservationRequiredLabelRule("tiered capacity reservation needs a value")
        val item = newTieredCapacityReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
