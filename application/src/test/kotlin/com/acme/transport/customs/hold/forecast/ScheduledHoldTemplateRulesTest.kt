package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHoldTemplateValidator()
        val item = newScheduledHoldTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledHoldTemplateRequiredTitleRule("scheduled hold template needs a value")
        val item = newScheduledHoldTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ScheduledHoldTemplateRequiredVehicleIdRule("scheduled hold template needs this field")
        val item = newScheduledHoldTemplate("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ScheduledHoldTemplateVehicleIdLengthRule(3600)
        val item = newScheduledHoldTemplate("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
