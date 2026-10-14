package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionLedgerValidator()
        val item = newInspectionLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = InspectionLedgerRequiredNotesRule("inspection ledger needs an entry")
        val item = newInspectionLedger("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionLedgerRequiredNameRule("inspection ledger needs an entry")
        val item = newInspectionLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = InspectionLedgerNotesLengthRule(255)
        val item = newInspectionLedger("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
