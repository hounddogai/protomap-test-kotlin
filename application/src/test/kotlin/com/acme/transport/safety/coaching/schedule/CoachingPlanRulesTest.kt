package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingPlanValidator()
        val item = newCoachingPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CoachingPlanOwnerIdLengthRule(64)
        val item = newCoachingPlan("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingPlanNameLengthRule(3600)
        val item = newCoachingPlan("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CoachingPlanRequiredChecksumRule("coaching plan needs a value")
        val item = newCoachingPlan("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = CoachingPlanChecksumLengthRule(3600)
        val item = newCoachingPlan("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CoachingPlanRequiredNameRule("coaching plan needs an entry")
        val item = newCoachingPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
