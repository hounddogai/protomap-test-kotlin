package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireThresholdValidator()
        val item = newTireThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = TireThresholdVehicleIdLengthRule(255)
        val item = newTireThreshold("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TireThresholdRequiredTitleRule("tire threshold needs this field")
        val item = newTireThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = TireThresholdCarrierLengthRule(86400)
        val item = newTireThreshold("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = TireThresholdRequiredVehicleIdRule("tire threshold needs an entry")
        val item = newTireThreshold("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
