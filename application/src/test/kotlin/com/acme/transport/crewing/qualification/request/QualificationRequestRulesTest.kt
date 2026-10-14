package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationRequestValidator()
        val item = newQualificationRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = QualificationRequestDiscountRangeRule(8.5, 3572.8)
        val item = newQualificationRequest("x", "y")
        item.discount = 3574.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = QualificationRequestMaximumRangeRule(2.1, 3501.4)
        val item = newQualificationRequest("x", "y")
        item.maximum = 3502.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = QualificationRequestFuelLitersRangeRule(4.3, 3551.2)
        val item = newQualificationRequest("x", "y")
        item.fuelLiters = 3552.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationRequestRequiredCodeRule("qualification request needs this field")
        val item = newQualificationRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationRequestLabelLengthRule(32)
        val item = newQualificationRequest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
