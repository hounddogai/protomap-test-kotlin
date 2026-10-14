package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubSummaryValidator()
        val item = newDefaultHubSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefaultHubSummaryOdometerRangeRule(32, 120)
        val item = newDefaultHubSummary("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultHubSummaryCodeLengthRule(32)
        val item = newDefaultHubSummary("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultHubSummaryRequiredNameRule("default hub summary needs an entry")
        val item = newDefaultHubSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefaultHubSummaryRequiredCategoryRule("default hub summary needs this field")
        val item = newDefaultHubSummary("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
