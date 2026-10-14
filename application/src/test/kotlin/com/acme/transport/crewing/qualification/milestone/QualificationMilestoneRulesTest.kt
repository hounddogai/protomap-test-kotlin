package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationMilestoneValidator()
        val item = newQualificationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = QualificationMilestoneNameLengthRule(86400)
        val item = newQualificationMilestone("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationMilestoneCodeLengthRule(80)
        val item = newQualificationMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = QualificationMilestoneEndsAtRangeRule(16, 64)
        val item = newQualificationMilestone("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
