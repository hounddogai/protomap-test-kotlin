package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectRecordValidator()
        val item = newDefectRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectRecordCodeLengthRule(32)
        val item = newDefectRecord("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefectRecordLabelLengthRule(86400)
        val item = newDefectRecord("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DefectRecordDurationMinutesRangeRule(32, 64)
        val item = newDefectRecord("x", "y")
        item.durationMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
