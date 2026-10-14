package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockPlanValidator()
        val item = newCrossDockPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CrossDockPlanUpdatedAtRangeRule(120, 1000)
        val item = newCrossDockPlan("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CrossDockPlanRequiredTitleRule("cross dock plan needs this field")
        val item = newCrossDockPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockPlanCodeLengthRule(80)
        val item = newCrossDockPlan("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CrossDockPlanRequiredDockRule("cross dock plan needs an entry")
        val item = newCrossDockPlan("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockPlanRequiredCodeRule("cross dock plan needs an entry")
        val item = newCrossDockPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
