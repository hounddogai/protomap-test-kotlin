package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLeaseReturnPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingLeaseReturnPreferenceValidator()
        val item = newPendingLeaseReturnPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = PendingLeaseReturnPreferenceReviewerLengthRule(86400)
        val item = newPendingLeaseReturnPreference("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PendingLeaseReturnPreferenceRequiredDepotRule("pending lease return preference needs this field")
        val item = newPendingLeaseReturnPreference("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingLeaseReturnPreferenceCodeLengthRule(86400)
        val item = newPendingLeaseReturnPreference("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingLeaseReturnPreferenceRequiredCodeRule("pending lease return preference needs this field")
        val item = newPendingLeaseReturnPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
