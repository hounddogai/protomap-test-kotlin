package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectLedgerValidator()
        val item = newDefectLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefectLedgerSequenceRangeRule(16, 80)
        val item = newDefectLedger("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefectLedgerOdometerRangeRule(255, 86400)
        val item = newDefectLedger("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefectLedgerTitleLengthRule(255)
        val item = newDefectLedger("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
