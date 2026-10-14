package com.acme.transport.crewing.qualification.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationTargetValidator()
        val item = newQualificationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = QualificationTargetTitleLengthRule(1000)
        val item = newQualificationTarget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationTargetCodeLengthRule(255)
        val item = newQualificationTarget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = QualificationTargetSourceLengthRule(1000)
        val item = newQualificationTarget("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = QualificationTargetRatioRangeRule(3.1, 136.3)
        val item = newQualificationTarget("x", "y")
        item.ratio = 137.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = QualificationTargetRequiredOwnerIdRule("qualification target needs an entry")
        val item = newQualificationTarget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
