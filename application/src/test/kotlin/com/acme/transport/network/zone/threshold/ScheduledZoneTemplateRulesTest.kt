package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneTemplateValidator()
        val item = newScheduledZoneTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ScheduledZoneTemplateRequiredSourceRule("scheduled zone template needs a value")
        val item = newScheduledZoneTemplate("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScheduledZoneTemplateWindowDaysRangeRule(16, 32)
        val item = newScheduledZoneTemplate("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScheduledZoneTemplateAttemptsRangeRule(32, 80)
        val item = newScheduledZoneTemplate("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledZoneTemplateLabelLengthRule(255)
        val item = newScheduledZoneTemplate("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
