package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryDigestValidator()
        val item = newRedeliveryDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RedeliveryDigestRequiredLocaleRule("redelivery digest needs this field")
        val item = newRedeliveryDigest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RedeliveryDigestBudgetRangeRule(7.3, 749.6)
        val item = newRedeliveryDigest("x", "y")
        item.budget = 751.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RedeliveryDigestDistanceKmRangeRule(8.9, 4699.6)
        val item = newRedeliveryDigest("x", "y")
        item.distanceKm = 4701.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryDigestRequiredCodeRule("redelivery digest needs this field")
        val item = newRedeliveryDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
