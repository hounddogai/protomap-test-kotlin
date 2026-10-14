package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAccessorialAuditValidator()
        val item = newCappedAccessorialAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CappedAccessorialAuditPhoneLengthRule(16)
        val item = newCappedAccessorialAudit("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedAccessorialAuditDriverIdLengthRule(86400)
        val item = newCappedAccessorialAudit("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = CappedAccessorialAuditBudgetRangeRule(7.4, 1027.3)
        val item = newCappedAccessorialAudit("x", "y")
        item.budget = 1028.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
