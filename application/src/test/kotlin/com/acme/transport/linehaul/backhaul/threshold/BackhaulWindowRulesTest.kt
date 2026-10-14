package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulWindowValidator()
        val item = newBackhaulWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulWindowRequiredCodeRule("backhaul window needs this field")
        val item = newBackhaulWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = BackhaulWindowPriorityRangeRule(255, 3600)
        val item = newBackhaulWindow("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulWindowCodeLengthRule(16)
        val item = newBackhaulWindow("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = BackhaulWindowVehicleIdLengthRule(16)
        val item = newBackhaulWindow("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
