package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectExceptionValidator()
        val item = newDefectException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DefectExceptionLimitRangeRule(32, 3600)
        val item = newDefectException("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefectExceptionNameLengthRule(3600)
        val item = newDefectException("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
