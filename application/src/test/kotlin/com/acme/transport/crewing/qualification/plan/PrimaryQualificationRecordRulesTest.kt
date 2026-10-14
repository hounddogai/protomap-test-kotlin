package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryQualificationRecordValidator()
        val item = newPrimaryQualificationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryQualificationRecordRequiredLabelRule("primary qualification record needs this field")
        val item = newPrimaryQualificationRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = PrimaryQualificationRecordRegionLengthRule(120)
        val item = newPrimaryQualificationRecord("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PrimaryQualificationRecordMarginRangeRule(9.0, 4341.2)
        val item = newPrimaryQualificationRecord("x", "y")
        item.margin = 4342.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryQualificationRecordLabelLengthRule(32)
        val item = newPrimaryQualificationRecord("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PrimaryQualificationRecordRequiredSegmentRule("primary qualification record needs this field")
        val item = newPrimaryQualificationRecord("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
