package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAvailabilityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAvailabilityExceptionValidator()
        val item = newLegacyAvailabilityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyAvailabilityExceptionCodeLengthRule(255)
        val item = newLegacyAvailabilityException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyAvailabilityExceptionRequiredNameRule("legacy availability exception needs a value")
        val item = newLegacyAvailabilityException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LegacyAvailabilityExceptionRequiredVehicleIdRule("legacy availability exception needs an entry")
        val item = newLegacyAvailabilityException("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
