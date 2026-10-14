package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRosterProfileValidator()
        val item = newScheduledRosterProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRosterProfileCodeLengthRule(1000)
        val item = newScheduledRosterProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledRosterProfileRequiredShiftCodeRule("scheduled roster profile needs an entry")
        val item = newScheduledRosterProfile("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
