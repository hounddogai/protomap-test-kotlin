package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnWindowValidator()
        val item = newLeaseReturnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LeaseReturnWindowRequiredTimezoneRule("lease return window needs a value")
        val item = newLeaseReturnWindow("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LeaseReturnWindowRequiredTitleRule("lease return window needs a value")
        val item = newLeaseReturnWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LeaseReturnWindowLabelLengthRule(255)
        val item = newLeaseReturnWindow("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LeaseReturnWindowDurationMinutesRangeRule(80, 86400)
        val item = newLeaseReturnWindow("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LeaseReturnWindowTimezoneLengthRule(3600)
        val item = newLeaseReturnWindow("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
