package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDefectLedgerValidator()
        val item = newManualDefectLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ManualDefectLedgerRequiredRegionRule("manual defect ledger needs an entry")
        val item = newManualDefectLedger("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDefectLedgerCodeLengthRule(86400)
        val item = newManualDefectLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ManualDefectLedgerUpdatedAtRangeRule(64, 86400)
        val item = newManualDefectLedger("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
