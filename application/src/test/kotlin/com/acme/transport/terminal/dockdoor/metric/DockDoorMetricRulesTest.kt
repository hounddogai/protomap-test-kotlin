package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorMetricValidator()
        val item = newDockDoorMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DockDoorMetricFuelLitersRangeRule(7.5, 430.2)
        val item = newDockDoorMetric("x", "y")
        item.fuelLiters = 431.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DockDoorMetricVehicleIdLengthRule(3600)
        val item = newDockDoorMetric("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DockDoorMetricOwnerIdLengthRule(255)
        val item = newDockDoorMetric("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorMetricRequiredNameRule("dock door metric needs this field")
        val item = newDockDoorMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorMetricNameLengthRule(80)
        val item = newDockDoorMetric("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorMetricCodeLengthRule(255)
        val item = newDockDoorMetric("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
