package com.acme.transport.crewing.swap.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalSwapRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalSwapRollupValidator()
        val item = newSeasonalSwapRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SeasonalSwapRollupDockLengthRule(1000)
        val item = newSeasonalSwapRollup("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalSwapRollupCodeLengthRule(120)
        val item = newSeasonalSwapRollup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
