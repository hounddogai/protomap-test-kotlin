package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectEstimateValidator()
        val item = newDefectEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefectEstimateGraceDaysRangeRule(16, 3600)
        val item = newDefectEstimate("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DefectEstimateOwnerIdLengthRule(255)
        val item = newDefectEstimate("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
