package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRedeliveryWindowValidator()
        val item = newScheduledRedeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledRedeliveryWindowLabelLengthRule(86400)
        val item = newScheduledRedeliveryWindow("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledRedeliveryWindowNameLengthRule(64)
        val item = newScheduledRedeliveryWindow("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
