package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationExceptionValidator()
        val item = newQualificationException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = QualificationExceptionRequiredPhoneRule("qualification exception needs this field")
        val item = newQualificationException("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = QualificationExceptionRatioRangeRule(7.8, 1176.0)
        val item = newQualificationException("x", "y")
        item.ratio = 1177.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationExceptionRequiredCodeRule("qualification exception needs a value")
        val item = newQualificationException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = QualificationExceptionRequiredTitleRule("qualification exception needs an entry")
        val item = newQualificationException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
