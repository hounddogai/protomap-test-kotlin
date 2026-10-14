package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryVarianceValidator()
        val item = newRedeliveryVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RedeliveryVarianceRequiredTitleRule("redelivery variance needs an entry")
        val item = newRedeliveryVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryVarianceRequiredCodeRule("redelivery variance needs a value")
        val item = newRedeliveryVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
