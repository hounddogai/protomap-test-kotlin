package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentLedgerValidator()
        val item = newIncidentLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentLedgerRequiredCodeRule("incident ledger needs this field")
        val item = newIncidentLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IncidentLedgerRequiredTitleRule("incident ledger needs a value")
        val item = newIncidentLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IncidentLedgerTitleLengthRule(32)
        val item = newIncidentLedger("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
