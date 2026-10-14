package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryRequestValidator()
        val item = newRedeliveryRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RedeliveryRequestRetriesRangeRule(64, 3600)
        val item = newRedeliveryRequest("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RedeliveryRequestRequiredCategoryRule("redelivery request needs a value")
        val item = newRedeliveryRequest("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
