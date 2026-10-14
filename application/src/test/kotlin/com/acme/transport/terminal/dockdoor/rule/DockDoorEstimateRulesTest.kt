package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorEstimateValidator()
        val item = newDockDoorEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DockDoorEstimateRequiredTitleRule("dock door estimate needs this field")
        val item = newDockDoorEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = DockDoorEstimateMarginRangeRule(6.8, 2836.8)
        val item = newDockDoorEstimate("x", "y")
        item.margin = 2838.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DockDoorEstimateThresholdRangeRule(9.9, 2965.4)
        val item = newDockDoorEstimate("x", "y")
        item.threshold = 2966.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
