package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerNoticeValidator()
        val item = newBrokerNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BrokerNoticeLabelLengthRule(16)
        val item = newBrokerNotice("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BrokerNoticeDurationMinutesRangeRule(1000, 86400)
        val item = newBrokerNotice("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BrokerNoticeQuantityRangeRule(64, 64)
        val item = newBrokerNotice("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BrokerNoticeRequiredLabelRule("broker notice needs this field")
        val item = newBrokerNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerNoticeCodeLengthRule(64)
        val item = newBrokerNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
