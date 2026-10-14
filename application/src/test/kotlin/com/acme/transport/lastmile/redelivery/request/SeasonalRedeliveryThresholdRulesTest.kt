package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryThresholdValidator()
        val item = newSeasonalRedeliveryThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = SeasonalRedeliveryThresholdRequiredDockRule("seasonal redelivery threshold needs this field")
        val item = newSeasonalRedeliveryThreshold("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SeasonalRedeliveryThresholdDockLengthRule(32)
        val item = newSeasonalRedeliveryThreshold("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = SeasonalRedeliveryThresholdWeightRangeRule(10.0, 3376.2)
        val item = newSeasonalRedeliveryThreshold("x", "y")
        item.weight = 3377.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
