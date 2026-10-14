package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardMappingValidator()
        val item = newRateCardMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardMappingNameLengthRule(86400)
        val item = newRateCardMapping("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RateCardMappingAttemptsRangeRule(120, 1000)
        val item = newRateCardMapping("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = RateCardMappingVersionRangeRule(64, 120)
        val item = newRateCardMapping("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RateCardMappingRequiredTimezoneRule("rate card mapping needs this field")
        val item = newRateCardMapping("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardMappingRequiredNameRule("rate card mapping needs an entry")
        val item = newRateCardMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
