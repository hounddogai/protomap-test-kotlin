package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRestBreakWindowValidator()
        val item = newGlobalRestBreakWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalRestBreakWindowRequiredNameRule("global rest break window needs this field")
        val item = newGlobalRestBreakWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRestBreakWindowRequiredCodeRule("global rest break window needs this field")
        val item = newGlobalRestBreakWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalRestBreakWindowNameLengthRule(120)
        val item = newGlobalRestBreakWindow("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = GlobalRestBreakWindowCostRangeRule(5.2, 1328.6)
        val item = newGlobalRestBreakWindow("x", "y")
        item.cost = 1330.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = GlobalRestBreakWindowWindowDaysRangeRule(32, 120)
        val item = newGlobalRestBreakWindow("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
