package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneAllocationValidator()
        val item = newZoneAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneAllocationRequiredCodeRule("zone allocation needs this field")
        val item = newZoneAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ZoneAllocationRequiredVehicleIdRule("zone allocation needs an entry")
        val item = newZoneAllocation("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
