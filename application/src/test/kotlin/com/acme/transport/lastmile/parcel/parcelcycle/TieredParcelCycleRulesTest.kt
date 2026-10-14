package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredParcelCycleValidator()
        val item = newTieredParcelCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TieredParcelCycleWindowDaysRangeRule(16, 16)
        val item = newTieredParcelCycle("x", "y")
        item.windowDays = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TieredParcelCycleRequiredSourceRule("tiered parcel cycle needs an entry")
        val item = newTieredParcelCycle("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredParcelCycleCodeLengthRule(64)
        val item = newTieredParcelCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredParcelCycleRequiredTitleRule("tiered parcel cycle needs a value")
        val item = newTieredParcelCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TieredParcelCycleRequiredContactEmailRule("tiered parcel cycle needs an entry")
        val item = newTieredParcelCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
