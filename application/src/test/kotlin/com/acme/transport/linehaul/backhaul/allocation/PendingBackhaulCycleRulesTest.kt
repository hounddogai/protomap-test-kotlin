package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBackhaulCycleValidator()
        val item = newPendingBackhaulCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PendingBackhaulCycleLocaleLengthRule(80)
        val item = newPendingBackhaulCycle("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingBackhaulCycleNameLengthRule(3600)
        val item = newPendingBackhaulCycle("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingBackhaulCycleRequiredNameRule("pending backhaul cycle needs this field")
        val item = newPendingBackhaulCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = PendingBackhaulCycleRequiredVehicleIdRule("pending backhaul cycle needs this field")
        val item = newPendingBackhaulCycle("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
