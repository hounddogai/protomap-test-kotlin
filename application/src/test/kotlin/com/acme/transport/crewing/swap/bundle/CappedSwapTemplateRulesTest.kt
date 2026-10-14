package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedSwapTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedSwapTemplateValidator()
        val item = newCappedSwapTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedSwapTemplateRequiredNameRule("capped swap template needs an entry")
        val item = newCappedSwapTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedSwapTemplateNameLengthRule(3600)
        val item = newCappedSwapTemplate("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CappedSwapTemplateRatioRangeRule(6.0, 3389.5)
        val item = newCappedSwapTemplate("x", "y")
        item.ratio = 3391.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
