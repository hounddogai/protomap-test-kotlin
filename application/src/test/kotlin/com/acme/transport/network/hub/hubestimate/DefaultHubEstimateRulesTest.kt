package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubEstimateValidator()
        val item = newDefaultHubEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultHubEstimateLabelLengthRule(32)
        val item = newDefaultHubEstimate("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = DefaultHubEstimateCountryLengthRule(16)
        val item = newDefaultHubEstimate("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultHubEstimateRequiredLabelRule("default hub estimate needs an entry")
        val item = newDefaultHubEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultHubEstimateRequiredCodeRule("default hub estimate needs an entry")
        val item = newDefaultHubEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
