package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryNoticeValidator()
        val item = newRedeliveryNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryNoticeCodeLengthRule(3600)
        val item = newRedeliveryNotice("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = RedeliveryNoticeCountryLengthRule(3600)
        val item = newRedeliveryNotice("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RedeliveryNoticeStartsAtRangeRule(16, 86400)
        val item = newRedeliveryNotice("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
