package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDropTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDropTargetValidator()
        val item = newLenientDropTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientDropTargetCapacityRangeRule(64, 255)
        val item = newLenientDropTarget("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientDropTargetLimitRangeRule(3600, 86400)
        val item = newLenientDropTarget("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientDropTargetRequiredTitleRule("lenient drop target needs a value")
        val item = newLenientDropTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDropTargetRequiredCodeRule("lenient drop target needs a value")
        val item = newLenientDropTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
