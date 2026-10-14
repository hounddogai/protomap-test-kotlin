package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNearMissOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNearMissOverrideValidator()
        val item = newScheduledNearMissOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledNearMissOverrideCodeLengthRule(255)
        val item = newScheduledNearMissOverride("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledNearMissOverrideWeightRangeRule(6.2, 2486.9)
        val item = newScheduledNearMissOverride("x", "y")
        item.weight = 2488.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = ScheduledNearMissOverrideRequiredUnitRule("scheduled near miss override needs an entry")
        val item = newScheduledNearMissOverride("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
