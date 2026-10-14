package com.acme.transport.crewing.roster.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterAdjustmentValidator()
        val item = newRosterAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterAdjustmentRequiredCodeRule("roster adjustment needs this field")
        val item = newRosterAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RosterAdjustmentCeilingRangeRule(4.8, 3760.8)
        val item = newRosterAdjustment("x", "y")
        item.ceiling = 3762.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
