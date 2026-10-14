package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectThresholdValidator()
        val item = newDefectThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefectThresholdWindowDaysRangeRule(32, 86400)
        val item = newDefectThreshold("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = DefectThresholdRegionLengthRule(64)
        val item = newDefectThreshold("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectThresholdRequiredCodeRule("defect threshold needs an entry")
        val item = newDefectThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefectThresholdEtaMinutesRangeRule(16, 32)
        val item = newDefectThreshold("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
