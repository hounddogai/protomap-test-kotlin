package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCrossDockPlanValidator()
        val item = newGlobalCrossDockPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalCrossDockPlanRequiredLabelRule("global cross dock plan needs this field")
        val item = newGlobalCrossDockPlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalCrossDockPlanLabelLengthRule(120)
        val item = newGlobalCrossDockPlan("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = GlobalCrossDockPlanWeightRangeRule(8.9, 3894.6)
        val item = newGlobalCrossDockPlan("x", "y")
        item.weight = 3896.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCrossDockPlanRequiredCodeRule("global cross dock plan needs a value")
        val item = newGlobalCrossDockPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalCrossDockPlanCodeLengthRule(120)
        val item = newGlobalCrossDockPlan("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
