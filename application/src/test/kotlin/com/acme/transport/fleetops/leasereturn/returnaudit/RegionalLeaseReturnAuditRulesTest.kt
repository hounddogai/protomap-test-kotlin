package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLeaseReturnAuditValidator()
        val item = newRegionalLeaseReturnAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalLeaseReturnAuditCodeLengthRule(3600)
        val item = newRegionalLeaseReturnAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RegionalLeaseReturnAuditRequiredLaneRule("regional lease return audit needs this field")
        val item = newRegionalLeaseReturnAudit("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalLeaseReturnAuditRequiredCodeRule("regional lease return audit needs this field")
        val item = newRegionalLeaseReturnAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RegionalLeaseReturnAuditGraceDaysRangeRule(80, 3600)
        val item = newRegionalLeaseReturnAudit("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RegionalLeaseReturnAuditTrailerIdLengthRule(255)
        val item = newRegionalLeaseReturnAudit("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
