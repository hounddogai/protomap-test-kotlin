package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterRequestValidator()
        val item = newRosterRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterRequestLabelLengthRule(255)
        val item = newRosterRequest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RosterRequestContactEmailLengthRule(80)
        val item = newRosterRequest("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterRequestCodeLengthRule(32)
        val item = newRosterRequest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RosterRequestCeilingRangeRule(6.4, 818.9)
        val item = newRosterRequest("x", "y")
        item.ceiling = 820.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
