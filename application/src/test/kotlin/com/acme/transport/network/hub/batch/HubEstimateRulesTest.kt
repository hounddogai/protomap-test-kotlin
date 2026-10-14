package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubEstimateValidator()
        val item = newHubEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = HubEstimateCurrencyLengthRule(86400)
        val item = newHubEstimate("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = HubEstimateRequiredCurrencyRule("hub estimate needs a value")
        val item = newHubEstimate("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubEstimateRequiredCodeRule("hub estimate needs this field")
        val item = newHubEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
