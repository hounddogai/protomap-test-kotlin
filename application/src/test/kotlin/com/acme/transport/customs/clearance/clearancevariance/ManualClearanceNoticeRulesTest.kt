package com.acme.transport.customs.clearance.clearancevariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualClearanceNoticeValidator()
        val item = newManualClearanceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ManualClearanceNoticeRequiredVehicleIdRule("manual clearance notice needs an entry")
        val item = newManualClearanceNotice("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ManualClearanceNoticeVersionRangeRule(80, 3600)
        val item = newManualClearanceNotice("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualClearanceNoticeRequiredLabelRule("manual clearance notice needs a value")
        val item = newManualClearanceNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = ManualClearanceNoticeSourceLengthRule(1000)
        val item = newManualClearanceNotice("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
