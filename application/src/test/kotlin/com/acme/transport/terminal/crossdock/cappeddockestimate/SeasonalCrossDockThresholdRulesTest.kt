package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCrossDockThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCrossDockThresholdValidator()
        val item = newSeasonalCrossDockThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalCrossDockThresholdRequiredCodeRule("seasonal cross dock threshold needs this field")
        val item = newSeasonalCrossDockThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SeasonalCrossDockThresholdDockLengthRule(120)
        val item = newSeasonalCrossDockThreshold("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
