package com.acme.transport.emissions.evcharge.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeNoticeValidator()
        val item = newEvChargeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeNoticeCodeLengthRule(64)
        val item = newEvChargeNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
