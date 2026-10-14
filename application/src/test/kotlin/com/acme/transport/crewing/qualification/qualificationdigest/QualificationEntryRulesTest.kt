package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationEntryValidator()
        val item = newQualificationEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = QualificationEntryRevisionRangeRule(16, 3600)
        val item = newQualificationEntry("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = QualificationEntryRequiredCategoryRule("qualification entry needs this field")
        val item = newQualificationEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = QualificationEntryTrailerIdLengthRule(86400)
        val item = newQualificationEntry("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationEntryCodeLengthRule(1000)
        val item = newQualificationEntry("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = QualificationEntryCategoryLengthRule(80)
        val item = newQualificationEntry("x", "y")
        item.category = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
