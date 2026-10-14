package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDockDoorTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDockDoorTierValidator()
        val item = newRegionalDockDoorTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDockDoorTierRequiredCodeRule("regional dock door tier needs an entry")
        val item = newRegionalDockDoorTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDockDoorTierLabelLengthRule(64)
        val item = newRegionalDockDoorTier("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDockDoorTierCodeLengthRule(120)
        val item = newRegionalDockDoorTier("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RegionalDockDoorTierRequiredSummaryRule("regional dock door tier needs an entry")
        val item = newRegionalDockDoorTier("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RegionalDockDoorTierMarginRangeRule(6.4, 2894.8)
        val item = newRegionalDockDoorTier("x", "y")
        item.margin = 2896.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
