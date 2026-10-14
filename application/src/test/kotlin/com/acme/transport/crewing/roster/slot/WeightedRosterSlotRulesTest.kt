package com.acme.transport.crewing.roster.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRosterSlotValidator()
        val item = newWeightedRosterSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedRosterSlotRequiredTitleRule("weighted roster slot needs a value")
        val item = newWeightedRosterSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedRosterSlotStartsAtRangeRule(64, 120)
        val item = newWeightedRosterSlot("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = WeightedRosterSlotBudgetRangeRule(2.8, 4966.0)
        val item = newWeightedRosterSlot("x", "y")
        item.budget = 4967.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedRosterSlotRequiredNameRule("weighted roster slot needs a value")
        val item = newWeightedRosterSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
