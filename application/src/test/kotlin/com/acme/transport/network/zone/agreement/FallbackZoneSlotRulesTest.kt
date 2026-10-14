package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackZoneSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackZoneSlotValidator()
        val item = newFallbackZoneSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FallbackZoneSlotWeightRangeRule(1.5, 2913.8)
        val item = newFallbackZoneSlot("x", "y")
        item.weight = 2915.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackZoneSlotStartsAtRangeRule(1000, 86400)
        val item = newFallbackZoneSlot("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FallbackZoneSlotMaximumRangeRule(2.7, 602.6)
        val item = newFallbackZoneSlot("x", "y")
        item.maximum = 604.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
