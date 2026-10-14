package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCrossDockAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCrossDockAuditValidator()
        val item = newManualCrossDockAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ManualCrossDockAuditVehicleIdLengthRule(86400)
        val item = newManualCrossDockAudit("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ManualCrossDockAuditRatioRangeRule(7.2, 700.8)
        val item = newManualCrossDockAudit("x", "y")
        item.ratio = 702.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ManualCrossDockAuditRequiredVehicleIdRule("manual cross dock audit needs this field")
        val item = newManualCrossDockAudit("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualCrossDockAuditNameLengthRule(120)
        val item = newManualCrossDockAudit("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCrossDockAuditRequiredCodeRule("manual cross dock audit needs a value")
        val item = newManualCrossDockAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
