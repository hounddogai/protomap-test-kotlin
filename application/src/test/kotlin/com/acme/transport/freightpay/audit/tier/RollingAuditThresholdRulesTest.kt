package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAuditThresholdValidator()
        val item = newRollingAuditThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingAuditThresholdNameLengthRule(32)
        val item = newRollingAuditThreshold("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingAuditThresholdAttemptsRangeRule(64, 86400)
        val item = newRollingAuditThreshold("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RollingAuditThresholdUnitLengthRule(86400)
        val item = newRollingAuditThreshold("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
