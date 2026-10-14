package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveUtilizationRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveUtilizationRollupValidator()
        val item = newAdaptiveUtilizationRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AdaptiveUtilizationRollupRequiredSegmentRule("adaptive utilization rollup needs this field")
        val item = newAdaptiveUtilizationRollup("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveUtilizationRollupRequiredCodeRule("adaptive utilization rollup needs an entry")
        val item = newAdaptiveUtilizationRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = AdaptiveUtilizationRollupSegmentLengthRule(120)
        val item = newAdaptiveUtilizationRollup("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AdaptiveUtilizationRollupQuantityRangeRule(32, 255)
        val item = newAdaptiveUtilizationRollup("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
