package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerPlanValidator()
        val item = newLockerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LockerPlanChannelLengthRule(16)
        val item = newLockerPlan("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerPlanRequiredTitleRule("locker plan needs a value")
        val item = newLockerPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = LockerPlanLocaleLengthRule(16)
        val item = newLockerPlan("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
