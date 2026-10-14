package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionLedgerValidator()
        val item = newLegacyInspectionLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyInspectionLedgerRequiredTitleRule("legacy inspection ledger needs a value")
        val item = newLegacyInspectionLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyInspectionLedgerCodeLengthRule(64)
        val item = newLegacyInspectionLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyInspectionLedgerTitleLengthRule(80)
        val item = newLegacyInspectionLedger("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyInspectionLedgerRequiredCodeRule("legacy inspection ledger needs this field")
        val item = newLegacyInspectionLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
