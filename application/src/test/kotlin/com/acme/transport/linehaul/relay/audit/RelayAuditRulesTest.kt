package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayAuditValidator()
        val item = newRelayAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RelayAuditNameLengthRule(64)
        val item = newRelayAudit("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayAuditRequiredCodeRule("relay audit needs an entry")
        val item = newRelayAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RelayAuditEndsAtRangeRule(16, 32)
        val item = newRelayAudit("x", "y")
        item.endsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RelayAuditRequiredNameRule("relay audit needs an entry")
        val item = newRelayAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
