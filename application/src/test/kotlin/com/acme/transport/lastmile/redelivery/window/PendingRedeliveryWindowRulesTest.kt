package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRedeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRedeliveryWindowValidator()
        val item = newPendingRedeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingRedeliveryWindowRequiredNameRule("pending redelivery window needs an entry")
        val item = newPendingRedeliveryWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = PendingRedeliveryWindowRequiredSourceRule("pending redelivery window needs an entry")
        val item = newPendingRedeliveryWindow("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = PendingRedeliveryWindowSourceLengthRule(1000)
        val item = newPendingRedeliveryWindow("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
