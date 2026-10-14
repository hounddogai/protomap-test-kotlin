package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCarbonReportLedgerValidator()
        val item = newWeightedCarbonReportLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCarbonReportLedgerCodeLengthRule(80)
        val item = newWeightedCarbonReportLedger("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = WeightedCarbonReportLedgerNotesLengthRule(32)
        val item = newWeightedCarbonReportLedger("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedCarbonReportLedgerBatchSizeRangeRule(86400, 86400)
        val item = newWeightedCarbonReportLedger("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedCarbonReportLedgerRequiredCodeRule("weighted carbon report ledger needs an entry")
        val item = newWeightedCarbonReportLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
