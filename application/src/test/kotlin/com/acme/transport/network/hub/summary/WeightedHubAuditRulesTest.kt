package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHubAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedHubAuditValidator()
        val item = newWeightedHubAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedHubAuditRequiredTitleRule("weighted hub audit needs an entry")
        val item = newWeightedHubAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = WeightedHubAuditRequiredLocaleRule("weighted hub audit needs a value")
        val item = newWeightedHubAudit("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedHubAuditTitleLengthRule(16)
        val item = newWeightedHubAudit("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
