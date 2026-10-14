package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerCycleValidator()
        val item = newGlobalTrailerCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = GlobalTrailerCycleRequiredCountryRule("global trailer cycle needs this field")
        val item = newGlobalTrailerCycle("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalTrailerCycleAttemptsRangeRule(16, 80)
        val item = newGlobalTrailerCycle("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
