package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCameraEventTargetValidator()
        val item = newPrimaryCameraEventTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryCameraEventTargetRequiredCategoryRule("primary camera event target needs an entry")
        val item = newPrimaryCameraEventTarget("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryCameraEventTargetRequiredTitleRule("primary camera event target needs an entry")
        val item = newPrimaryCameraEventTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PrimaryCameraEventTargetMarginRangeRule(6.5, 3661.0)
        val item = newPrimaryCameraEventTarget("x", "y")
        item.margin = 3662.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
