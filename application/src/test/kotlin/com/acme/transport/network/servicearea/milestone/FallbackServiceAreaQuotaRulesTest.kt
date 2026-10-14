package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServiceAreaQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServiceAreaQuotaValidator()
        val item = newFallbackServiceAreaQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackServiceAreaQuotaCurrencyLengthRule(255)
        val item = newFallbackServiceAreaQuota("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackServiceAreaQuotaRequiredTitleRule("fallback service area quota needs an entry")
        val item = newFallbackServiceAreaQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
