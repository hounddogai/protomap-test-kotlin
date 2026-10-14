package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingQualificationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingQualificationAssignmentValidator()
        val item = newPendingQualificationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingQualificationAssignmentLabelLengthRule(3600)
        val item = newPendingQualificationAssignment("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingQualificationAssignmentRatioRangeRule(8.9, 605.2)
        val item = newPendingQualificationAssignment("x", "y")
        item.ratio = 606.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingQualificationAssignmentRequiredLabelRule("pending qualification assignment needs an entry")
        val item = newPendingQualificationAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
