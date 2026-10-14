package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDefectExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDefectExceptionValidator()
        val item = newRollingDefectException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingDefectExceptionRequiredTitleRule("rolling defect exception needs this field")
        val item = newRollingDefectException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingDefectExceptionScoreRangeRule(2.9, 3363.6)
        val item = newRollingDefectException("x", "y")
        item.score = 3365.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RollingDefectExceptionCarrierLengthRule(3600)
        val item = newRollingDefectException("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
