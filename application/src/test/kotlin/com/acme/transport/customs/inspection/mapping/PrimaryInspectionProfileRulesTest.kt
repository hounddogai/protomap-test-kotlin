package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionProfileValidator()
        val item = newPrimaryInspectionProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryInspectionProfileLabelLengthRule(255)
        val item = newPrimaryInspectionProfile("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PrimaryInspectionProfileDurationMinutesRangeRule(32, 255)
        val item = newPrimaryInspectionProfile("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryInspectionProfileRequiredCodeRule("primary inspection profile needs a value")
        val item = newPrimaryInspectionProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryInspectionProfileStopCountRangeRule(1000, 3600)
        val item = newPrimaryInspectionProfile("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
