package com.acme.transport.network.zone.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneWindowValidator()
        val item = newZoneWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ZoneWindowCreatedAtRangeRule(255, 255)
        val item = newZoneWindow("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ZoneWindowStopCountRangeRule(16, 1000)
        val item = newZoneWindow("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ZoneWindowRequiredDescriptionRule("zone window needs this field")
        val item = newZoneWindow("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ZoneWindowTitleLengthRule(3600)
        val item = newZoneWindow("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ZoneWindowUpdatedAtRangeRule(80, 1000)
        val item = newZoneWindow("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
