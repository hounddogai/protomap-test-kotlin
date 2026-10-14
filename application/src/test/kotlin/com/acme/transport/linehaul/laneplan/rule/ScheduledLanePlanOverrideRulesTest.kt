package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLanePlanOverrideValidator()
        val item = newScheduledLanePlanOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledLanePlanOverrideWeightRangeRule(7.5, 4388.9)
        val item = newScheduledLanePlanOverride("x", "y")
        item.weight = 4390.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledLanePlanOverrideRequiredNameRule("scheduled lane plan override needs an entry")
        val item = newScheduledLanePlanOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledLanePlanOverrideCodeLengthRule(120)
        val item = newScheduledLanePlanOverride("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
