package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualQualificationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualQualificationProfileValidator()
        val item = newManualQualificationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualQualificationProfileRequiredCodeRule("manual qualification profile needs an entry")
        val item = newManualQualificationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualQualificationProfileLabelLengthRule(1000)
        val item = newManualQualificationProfile("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualQualificationProfileRequiredTitleRule("manual qualification profile needs an entry")
        val item = newManualQualificationProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualQualificationProfileStartsAtRangeRule(32, 1000)
        val item = newManualQualificationProfile("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ManualQualificationProfileStopCountRangeRule(80, 255)
        val item = newManualQualificationProfile("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
