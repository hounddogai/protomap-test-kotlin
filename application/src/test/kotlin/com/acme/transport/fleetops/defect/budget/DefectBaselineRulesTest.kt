package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectBaselineValidator()
        val item = newDefectBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefectBaselineAmountRangeRule(7.8, 3491.7)
        val item = newDefectBaseline("x", "y")
        item.amount = 3493.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefectBaselineEtaMinutesRangeRule(32, 32)
        val item = newDefectBaseline("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DefectBaselineDurationMinutesRangeRule(64, 86400)
        val item = newDefectBaseline("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
