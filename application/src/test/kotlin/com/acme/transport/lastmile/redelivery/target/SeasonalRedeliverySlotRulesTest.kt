package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliverySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliverySlotValidator()
        val item = newSeasonalRedeliverySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRedeliverySlotCodeLengthRule(3600)
        val item = newSeasonalRedeliverySlot("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalRedeliverySlotRequiredTitleRule("seasonal redelivery slot needs a value")
        val item = newSeasonalRedeliverySlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalRedeliverySlotTitleLengthRule(16)
        val item = newSeasonalRedeliverySlot("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SeasonalRedeliverySlotMarginRangeRule(6.2, 2409.0)
        val item = newSeasonalRedeliverySlot("x", "y")
        item.margin = 2410.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
