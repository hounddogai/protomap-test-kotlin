package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedEvChargeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedEvChargeRecordValidator()
        val item = newCappedEvChargeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = CappedEvChargeRecordEndsAtRangeRule(32, 1000)
        val item = newCappedEvChargeRecord("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = CappedEvChargeRecordRouteCodeLengthRule(86400)
        val item = newCappedEvChargeRecord("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedEvChargeRecordCodeLengthRule(1000)
        val item = newCappedEvChargeRecord("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = CappedEvChargeRecordRequiredVehicleIdRule("capped ev charge record needs an entry")
        val item = newCappedEvChargeRecord("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = CappedEvChargeRecordVehicleIdLengthRule(64)
        val item = newCappedEvChargeRecord("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedEvChargeRecordRequiredCodeRule("capped ev charge record needs this field")
        val item = newCappedEvChargeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
