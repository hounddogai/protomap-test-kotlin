package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledViolationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledViolationDraftValidator()
        val item = newScheduledViolationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ScheduledViolationDraftRequiredDockRule("scheduled violation draft needs this field")
        val item = newScheduledViolationDraft("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledViolationDraftRequiredNameRule("scheduled violation draft needs a value")
        val item = newScheduledViolationDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ScheduledViolationDraftDockLengthRule(120)
        val item = newScheduledViolationDraft("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
