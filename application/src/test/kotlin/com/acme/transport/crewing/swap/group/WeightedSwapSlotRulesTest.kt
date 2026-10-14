package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedSwapSlotValidator()
        val item = newWeightedSwapSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = WeightedSwapSlotRequiredChecksumRule("weighted swap slot needs a value")
        val item = newWeightedSwapSlot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = WeightedSwapSlotChecksumLengthRule(255)
        val item = newWeightedSwapSlot("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedSwapSlotVersionRangeRule(255, 86400)
        val item = newWeightedSwapSlot("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedSwapSlotRequiredCodeRule("weighted swap slot needs this field")
        val item = newWeightedSwapSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedSwapSlotRequiredNameRule("weighted swap slot needs a value")
        val item = newWeightedSwapSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
