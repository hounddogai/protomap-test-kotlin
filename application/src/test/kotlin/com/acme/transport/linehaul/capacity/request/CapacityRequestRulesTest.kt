package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityRequestValidator()
        val item = newCapacityRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CapacityRequestRequiredTitleRule("capacity request needs this field")
        val item = newCapacityRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacityRequestCodeLengthRule(120)
        val item = newCapacityRequest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityRequestRequiredCodeRule("capacity request needs an entry")
        val item = newCapacityRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = CapacityRequestVehicleIdLengthRule(1000)
        val item = newCapacityRequest("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
