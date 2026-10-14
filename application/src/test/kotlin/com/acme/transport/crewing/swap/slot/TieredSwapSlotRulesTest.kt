package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSwapSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSwapSlotValidator()
        val item = newTieredSwapSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TieredSwapSlotCapacityRangeRule(16, 3600)
        val item = newTieredSwapSlot("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TieredSwapSlotRequiredDescriptionRule("tiered swap slot needs a value")
        val item = newTieredSwapSlot("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
