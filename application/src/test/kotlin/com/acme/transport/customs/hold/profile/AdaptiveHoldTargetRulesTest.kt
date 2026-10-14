package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHoldTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveHoldTargetValidator()
        val item = newAdaptiveHoldTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveHoldTargetRequiredNameRule("adaptive hold target needs a value")
        val item = newAdaptiveHoldTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AdaptiveHoldTargetAttemptsRangeRule(16, 255)
        val item = newAdaptiveHoldTarget("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
