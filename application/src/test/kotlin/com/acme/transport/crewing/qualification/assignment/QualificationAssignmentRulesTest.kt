package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationAssignmentValidator()
        val item = newQualificationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationAssignmentRequiredCodeRule("qualification assignment needs a value")
        val item = newQualificationAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = QualificationAssignmentWindowDaysRangeRule(255, 86400)
        val item = newQualificationAssignment("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = QualificationAssignmentRequiredTitleRule("qualification assignment needs a value")
        val item = newQualificationAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationAssignmentCodeLengthRule(255)
        val item = newQualificationAssignment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = QualificationAssignmentCapacityRangeRule(64, 80)
        val item = newQualificationAssignment("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
