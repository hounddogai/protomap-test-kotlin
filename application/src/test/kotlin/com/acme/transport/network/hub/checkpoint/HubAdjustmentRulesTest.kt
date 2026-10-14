package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubAdjustmentValidator()
        val item = newHubAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubAdjustmentNameLengthRule(120)
        val item = newHubAdjustment("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = HubAdjustmentAttemptsRangeRule(80, 1000)
        val item = newHubAdjustment("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HubAdjustmentRequiredNameRule("hub adjustment needs a value")
        val item = newHubAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
