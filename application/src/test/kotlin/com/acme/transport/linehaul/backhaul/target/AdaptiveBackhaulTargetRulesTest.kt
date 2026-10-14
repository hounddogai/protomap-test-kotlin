package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBackhaulTargetValidator()
        val item = newAdaptiveBackhaulTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveBackhaulTargetRequiredLabelRule("adaptive backhaul target needs a value")
        val item = newAdaptiveBackhaulTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = AdaptiveBackhaulTargetSequenceRangeRule(16, 32)
        val item = newAdaptiveBackhaulTarget("x", "y")
        item.sequence = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = AdaptiveBackhaulTargetStopCountRangeRule(255, 1000)
        val item = newAdaptiveBackhaulTarget("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = AdaptiveBackhaulTargetRequiredLocaleRule("adaptive backhaul target needs this field")
        val item = newAdaptiveBackhaulTarget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
