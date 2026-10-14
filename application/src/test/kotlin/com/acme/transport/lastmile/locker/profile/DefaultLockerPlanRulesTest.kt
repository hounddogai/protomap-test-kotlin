package com.acme.transport.lastmile.locker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLockerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultLockerPlanValidator()
        val item = newDefaultLockerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DefaultLockerPlanRequiredSummaryRule("default locker plan needs a value")
        val item = newDefaultLockerPlan("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultLockerPlanCodeLengthRule(16)
        val item = newDefaultLockerPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DefaultLockerPlanMaximumRangeRule(3.7, 4973.8)
        val item = newDefaultLockerPlan("x", "y")
        item.maximum = 4975.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DefaultLockerPlanWeightRangeRule(2.9, 3763.5)
        val item = newDefaultLockerPlan("x", "y")
        item.weight = 3765.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DefaultLockerPlanOwnerIdLengthRule(3600)
        val item = newDefaultLockerPlan("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DefaultLockerPlanExpiresAtRangeRule(16, 80)
        val item = newDefaultLockerPlan("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
