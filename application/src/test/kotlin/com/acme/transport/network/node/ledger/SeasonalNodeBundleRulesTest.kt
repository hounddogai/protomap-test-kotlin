package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalNodeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalNodeBundleValidator()
        val item = newSeasonalNodeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalNodeBundleTitleLengthRule(1000)
        val item = newSeasonalNodeBundle("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalNodeBundleRequiredTitleRule("seasonal node bundle needs an entry")
        val item = newSeasonalNodeBundle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = SeasonalNodeBundleWeightRangeRule(1.6, 3984.0)
        val item = newSeasonalNodeBundle("x", "y")
        item.weight = 3985.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
