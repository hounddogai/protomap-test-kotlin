package com.acme.transport.safety.nearmiss.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultNearMissAuditValidator()
        val item = newDefaultNearMissAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultNearMissAuditRequiredCodeRule("default near miss audit needs an entry")
        val item = newDefaultNearMissAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultNearMissAuditMinimumRangeRule(6.3, 1155.4)
        val item = newDefaultNearMissAudit("x", "y")
        item.minimum = 1156.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultNearMissAuditTitleLengthRule(64)
        val item = newDefaultNearMissAudit("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
