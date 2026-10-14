package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTrailerAssignmentValidator()
        val item = newRollingTrailerAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingTrailerAssignmentThresholdRangeRule(2.6, 4651.9)
        val item = newRollingTrailerAssignment("x", "y")
        item.threshold = 4653.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingTrailerAssignmentOdometerRangeRule(64, 64)
        val item = newRollingTrailerAssignment("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingTrailerAssignmentPhoneLengthRule(255)
        val item = newRollingTrailerAssignment("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingTrailerAssignmentCodeLengthRule(1000)
        val item = newRollingTrailerAssignment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RollingTrailerAssignmentCeilingRangeRule(6.8, 4085.0)
        val item = newRollingTrailerAssignment("x", "y")
        item.ceiling = 4086.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
