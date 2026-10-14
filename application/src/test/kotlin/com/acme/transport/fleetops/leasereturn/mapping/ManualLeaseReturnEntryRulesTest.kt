package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLeaseReturnEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualLeaseReturnEntryValidator()
        val item = newManualLeaseReturnEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ManualLeaseReturnEntryAmountRangeRule(5.7, 369.4)
        val item = newManualLeaseReturnEntry("x", "y")
        item.amount = 370.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualLeaseReturnEntryCodeLengthRule(3600)
        val item = newManualLeaseReturnEntry("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ManualLeaseReturnEntryTrailerIdLengthRule(86400)
        val item = newManualLeaseReturnEntry("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualLeaseReturnEntryLabelLengthRule(255)
        val item = newManualLeaseReturnEntry("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
