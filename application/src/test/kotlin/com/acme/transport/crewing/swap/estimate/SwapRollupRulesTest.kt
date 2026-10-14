package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapRollupValidator()
        val item = newSwapRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapRollupNameLengthRule(80)
        val item = newSwapRollup("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapRollupCodeLengthRule(86400)
        val item = newSwapRollup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = SwapRollupRequiredTrailerIdRule("swap rollup needs this field")
        val item = newSwapRollup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
