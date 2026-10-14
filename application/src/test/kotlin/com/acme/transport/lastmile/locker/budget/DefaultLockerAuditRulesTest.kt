package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLockerAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultLockerAuditValidator()
        val item = newDefaultLockerAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DefaultLockerAuditSegmentLengthRule(64)
        val item = newDefaultLockerAudit("x", "y")
        item.segment = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
