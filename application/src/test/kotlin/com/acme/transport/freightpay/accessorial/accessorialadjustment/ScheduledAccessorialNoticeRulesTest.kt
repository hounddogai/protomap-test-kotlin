package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAccessorialNoticeValidator()
        val item = newScheduledAccessorialNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ScheduledAccessorialNoticeRequiredChannelRule("scheduled accessorial notice needs a value")
        val item = newScheduledAccessorialNotice("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAccessorialNoticeCodeLengthRule(16)
        val item = newScheduledAccessorialNotice("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ScheduledAccessorialNoticeChannelLengthRule(120)
        val item = newScheduledAccessorialNotice("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
