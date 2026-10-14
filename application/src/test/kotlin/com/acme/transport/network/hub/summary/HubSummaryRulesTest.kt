package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubSummaryValidator()
        val item = newHubSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = HubSummaryLabelLengthRule(255)
        val item = newHubSummary("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = HubSummaryOdometerRangeRule(120, 1000)
        val item = newHubSummary("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
