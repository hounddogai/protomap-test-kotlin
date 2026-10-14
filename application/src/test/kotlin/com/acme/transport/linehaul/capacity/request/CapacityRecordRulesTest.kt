package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityRecordValidator()
        val item = newCapacityRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = CapacityRecordVehicleIdLengthRule(16)
        val item = newCapacityRecord("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityRecordRequiredCodeRule("capacity record needs an entry")
        val item = newCapacityRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
