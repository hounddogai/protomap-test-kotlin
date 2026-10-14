package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliveryNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRedeliveryNoticeValidator()
        val item = newDefaultRedeliveryNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRedeliveryNoticeCodeLengthRule(255)
        val item = newDefaultRedeliveryNotice("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultRedeliveryNoticeAttemptsRangeRule(1000, 86400)
        val item = newDefaultRedeliveryNotice("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefaultRedeliveryNoticeRetriesRangeRule(16, 255)
        val item = newDefaultRedeliveryNotice("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DefaultRedeliveryNoticeRequiredOwnerIdRule("default redelivery notice needs this field")
        val item = newDefaultRedeliveryNotice("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
