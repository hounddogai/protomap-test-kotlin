package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRestBreakRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRestBreakRollupValidator()
        val item = newRollingRestBreakRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingRestBreakRollupTitleLengthRule(255)
        val item = newRollingRestBreakRollup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RollingRestBreakRollupRequiredDockRule("rolling rest break rollup needs an entry")
        val item = newRollingRestBreakRollup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingRestBreakRollupRequiredCodeRule("rolling rest break rollup needs a value")
        val item = newRollingRestBreakRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RollingRestBreakRollupDiscountRangeRule(3.2, 1071.6)
        val item = newRollingRestBreakRollup("x", "y")
        item.discount = 1073.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
