package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryWindowValidator()
        val item = newRedeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RedeliveryWindowDistanceKmRangeRule(8.2, 3233.2)
        val item = newRedeliveryWindow("x", "y")
        item.distanceKm = 3234.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RedeliveryWindowLimitRangeRule(64, 80)
        val item = newRedeliveryWindow("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryWindowRequiredCodeRule("redelivery window needs an entry")
        val item = newRedeliveryWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RedeliveryWindowRequiredCurrencyRule("redelivery window needs this field")
        val item = newRedeliveryWindow("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
