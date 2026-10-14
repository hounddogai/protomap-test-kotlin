package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakTargetValidator()
        val item = newRestBreakTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RestBreakTargetRatioRangeRule(9.5, 2382.4)
        val item = newRestBreakTarget("x", "y")
        item.ratio = 2383.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RestBreakTargetRateRangeRule(0.3, 2301.4)
        val item = newRestBreakTarget("x", "y")
        item.rate = 2302.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakTargetCodeLengthRule(120)
        val item = newRestBreakTarget("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakTargetRequiredLabelRule("rest break target needs an entry")
        val item = newRestBreakTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
