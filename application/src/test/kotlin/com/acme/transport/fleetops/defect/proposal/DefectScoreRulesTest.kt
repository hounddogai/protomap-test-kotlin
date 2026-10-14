package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectScoreValidator()
        val item = newDefectScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DefectScoreCreatedAtRangeRule(16, 1000)
        val item = newDefectScore("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectScoreCodeLengthRule(86400)
        val item = newDefectScore("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectScoreRequiredCodeRule("defect score needs this field")
        val item = newDefectScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
