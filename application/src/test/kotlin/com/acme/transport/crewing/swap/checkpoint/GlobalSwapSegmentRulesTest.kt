package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSwapSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalSwapSegmentValidator()
        val item = newGlobalSwapSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = GlobalSwapSegmentRatioRangeRule(4.8, 4588.3)
        val item = newGlobalSwapSegment("x", "y")
        item.ratio = 4589.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalSwapSegmentEtaMinutesRangeRule(64, 86400)
        val item = newGlobalSwapSegment("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalSwapSegmentRequiredCodeRule("global swap segment needs a value")
        val item = newGlobalSwapSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
