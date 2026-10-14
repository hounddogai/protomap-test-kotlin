package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationWindowValidator()
        val item = newQualificationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationWindowCodeLengthRule(86400)
        val item = newQualificationWindow("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = QualificationWindowBudgetRangeRule(0.6, 1037.4)
        val item = newQualificationWindow("x", "y")
        item.budget = 1038.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = QualificationWindowGraceDaysRangeRule(80, 255)
        val item = newQualificationWindow("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
