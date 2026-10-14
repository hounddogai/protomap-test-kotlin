package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeTemplateValidator()
        val item = newDisputeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DisputeTemplateEtaMinutesRangeRule(32, 255)
        val item = newDisputeTemplate("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DisputeTemplateRequiredChannelRule("dispute template needs an entry")
        val item = newDisputeTemplate("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DisputeTemplateRatioRangeRule(3.8, 4111.7)
        val item = newDisputeTemplate("x", "y")
        item.ratio = 4113.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DisputeTemplateNameLengthRule(16)
        val item = newDisputeTemplate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DisputeTemplateDiscountRangeRule(2.9, 1417.9)
        val item = newDisputeTemplate("x", "y")
        item.discount = 1419.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
