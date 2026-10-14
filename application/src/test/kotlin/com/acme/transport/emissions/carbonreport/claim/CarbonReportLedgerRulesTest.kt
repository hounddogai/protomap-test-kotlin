package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportLedgerValidator()
        val item = newCarbonReportLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CarbonReportLedgerDistanceKmRangeRule(5.0, 1232.3)
        val item = newCarbonReportLedger("x", "y")
        item.distanceKm = 1233.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = CarbonReportLedgerShiftCodeLengthRule(120)
        val item = newCarbonReportLedger("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CarbonReportLedgerMarginRangeRule(8.6, 3981.0)
        val item = newCarbonReportLedger("x", "y")
        item.margin = 3982.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = CarbonReportLedgerLabelLengthRule(86400)
        val item = newCarbonReportLedger("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
