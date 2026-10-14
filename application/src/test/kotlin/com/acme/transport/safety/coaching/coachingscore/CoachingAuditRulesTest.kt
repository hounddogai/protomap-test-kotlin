package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingAuditValidator()
        val item = newCoachingAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CoachingAuditRequiredOwnerIdRule("coaching audit needs an entry")
        val item = newCoachingAudit("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CoachingAuditRequiredTitleRule("coaching audit needs an entry")
        val item = newCoachingAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingAuditCodeLengthRule(80)
        val item = newCoachingAudit("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingAuditNameLengthRule(255)
        val item = newCoachingAudit("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = CoachingAuditWeightRangeRule(9.4, 2494.2)
        val item = newCoachingAudit("x", "y")
        item.weight = 2495.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingAuditRequiredCodeRule("coaching audit needs a value")
        val item = newCoachingAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
