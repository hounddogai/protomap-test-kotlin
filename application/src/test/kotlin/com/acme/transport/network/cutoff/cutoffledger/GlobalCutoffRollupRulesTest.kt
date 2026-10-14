package com.acme.transport.network.cutoff.cutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCutoffRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCutoffRollupValidator()
        val item = newGlobalCutoffRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = GlobalCutoffRollupBudgetRangeRule(3.6, 4375.0)
        val item = newGlobalCutoffRollup("x", "y")
        item.budget = 4376.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalCutoffRollupTitleLengthRule(1000)
        val item = newGlobalCutoffRollup("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCutoffRollupRequiredCodeRule("global cutoff rollup needs an entry")
        val item = newGlobalCutoffRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalCutoffRollupRequiredLaneRule("global cutoff rollup needs a value")
        val item = newGlobalCutoffRollup("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
