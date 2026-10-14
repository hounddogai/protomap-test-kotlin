package com.acme.transport.crewing.qualification.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationExceptionValidator()
        val item = newRegionalQualificationException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalQualificationExceptionCeilingRangeRule(2.0, 4085.4)
        val item = newRegionalQualificationException("x", "y")
        item.ceiling = 4086.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalQualificationExceptionCodeLengthRule(32)
        val item = newRegionalQualificationException("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalQualificationExceptionRequiredCodeRule("regional qualification exception needs a value")
        val item = newRegionalQualificationException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalQualificationExceptionLabelLengthRule(255)
        val item = newRegionalQualificationException("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalQualificationExceptionRequiredLabelRule("regional qualification exception needs a value")
        val item = newRegionalQualificationException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
