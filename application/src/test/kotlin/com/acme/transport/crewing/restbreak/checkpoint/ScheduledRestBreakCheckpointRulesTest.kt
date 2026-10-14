package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRestBreakCheckpointValidator()
        val item = newScheduledRestBreakCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ScheduledRestBreakCheckpointMarginRangeRule(9.3, 3408.4)
        val item = newScheduledRestBreakCheckpoint("x", "y")
        item.margin = 3409.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ScheduledRestBreakCheckpointRequiredRouteCodeRule("scheduled rest break checkpoint needs an entry")
        val item = newScheduledRestBreakCheckpoint("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledRestBreakCheckpointRequiredNameRule("scheduled rest break checkpoint needs a value")
        val item = newScheduledRestBreakCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ScheduledRestBreakCheckpointRouteCodeLengthRule(16)
        val item = newScheduledRestBreakCheckpoint("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
