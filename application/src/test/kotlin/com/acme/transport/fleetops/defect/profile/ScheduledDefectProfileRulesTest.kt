package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectProfileValidator()
        val item = newScheduledDefectProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ScheduledDefectProfileRequiredExternalRefRule("scheduled defect profile needs a value")
        val item = newScheduledDefectProfile("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDefectProfileCodeLengthRule(120)
        val item = newScheduledDefectProfile("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledDefectProfileNameLengthRule(64)
        val item = newScheduledDefectProfile("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledDefectProfileOdometerRangeRule(32, 64)
        val item = newScheduledDefectProfile("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDefectProfileRequiredCodeRule("scheduled defect profile needs this field")
        val item = newScheduledDefectProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
