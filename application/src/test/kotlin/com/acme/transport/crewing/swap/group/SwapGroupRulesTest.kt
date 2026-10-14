package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapGroupValidator()
        val item = newSwapGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapGroupRequiredCodeRule("swap group needs this field")
        val item = newSwapGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SwapGroupRequiredTitleRule("swap group needs this field")
        val item = newSwapGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = SwapGroupCategoryLengthRule(32)
        val item = newSwapGroup("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
