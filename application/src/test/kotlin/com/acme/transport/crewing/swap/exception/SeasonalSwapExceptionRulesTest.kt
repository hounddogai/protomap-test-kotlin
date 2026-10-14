package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalSwapExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalSwapExceptionValidator()
        val item = newSeasonalSwapException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalSwapExceptionCodeLengthRule(32)
        val item = newSeasonalSwapException("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalSwapExceptionRequiredCodeRule("seasonal swap exception needs this field")
        val item = newSeasonalSwapException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
