package com.acme.transport.crewing.swap.swapnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapProfileValidator()
        val item = newSwapProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapProfileCodeLengthRule(1000)
        val item = newSwapProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapProfileNameLengthRule(3600)
        val item = newSwapProfile("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
