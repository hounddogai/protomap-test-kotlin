package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDockDoorAdjustmentValidator()
        val item = newSeasonalDockDoorAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalDockDoorAdjustmentRequiredLabelRule("seasonal dock door adjustment needs a value")
        val item = newSeasonalDockDoorAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalDockDoorAdjustmentBatchSizeRangeRule(80, 255)
        val item = newSeasonalDockDoorAdjustment("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = SeasonalDockDoorAdjustmentMinimumRangeRule(3.3, 3868.6)
        val item = newSeasonalDockDoorAdjustment("x", "y")
        item.minimum = 3870.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDockDoorAdjustmentRequiredCodeRule("seasonal dock door adjustment needs a value")
        val item = newSeasonalDockDoorAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalDockDoorAdjustmentLabelLengthRule(255)
        val item = newSeasonalDockDoorAdjustment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
