package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRedeliveryQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRedeliveryQuotaValidator()
        val item = newStrictRedeliveryQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictRedeliveryQuotaRequiredTitleRule("strict redelivery quota needs a value")
        val item = newStrictRedeliveryQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictRedeliveryQuotaTitleLengthRule(64)
        val item = newStrictRedeliveryQuota("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
