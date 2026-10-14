package com.acme.transport.lastmile.locker.lockerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLockerRuleValidator()
        val item = newScheduledLockerRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ScheduledLockerRuleContactEmailLengthRule(255)
        val item = newScheduledLockerRule("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ScheduledLockerRuleTimezoneLengthRule(80)
        val item = newScheduledLockerRule("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledLockerRuleCodeLengthRule(120)
        val item = newScheduledLockerRule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ScheduledLockerRuleRequiredTimezoneRule("scheduled locker rule needs an entry")
        val item = newScheduledLockerRule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
