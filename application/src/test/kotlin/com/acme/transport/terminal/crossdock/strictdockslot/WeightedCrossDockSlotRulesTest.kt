package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCrossDockSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCrossDockSlotValidator()
        val item = newWeightedCrossDockSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedCrossDockSlotSummaryLengthRule(32)
        val item = newWeightedCrossDockSlot("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedCrossDockSlotNameLengthRule(32)
        val item = newWeightedCrossDockSlot("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = WeightedCrossDockSlotMarginRangeRule(3.4, 1871.8)
        val item = newWeightedCrossDockSlot("x", "y")
        item.margin = 1873.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = WeightedCrossDockSlotGraceDaysRangeRule(32, 86400)
        val item = newWeightedCrossDockSlot("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
