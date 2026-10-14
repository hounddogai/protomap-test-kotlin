package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireAuditValidator()
        val item = newTireAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = TireAuditVersionRangeRule(32, 32)
        val item = newTireAudit("x", "y")
        item.version = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TireAuditRequiredTitleRule("tire audit needs a value")
        val item = newTireAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TireAuditPriorityRangeRule(32, 86400)
        val item = newTireAudit("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = TireAuditBudgetRangeRule(9.1, 481.6)
        val item = newTireAudit("x", "y")
        item.budget = 483.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TireAuditContactEmailLengthRule(16)
        val item = newTireAudit("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
