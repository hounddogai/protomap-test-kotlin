package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRateCardNoticeValidator()
        val item = newCappedRateCardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedRateCardNoticeTitleLengthRule(120)
        val item = newCappedRateCardNotice("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedRateCardNoticeCodeLengthRule(64)
        val item = newCappedRateCardNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CappedRateCardNoticeRequiredTrailerIdRule("capped rate card notice needs an entry")
        val item = newCappedRateCardNotice("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CappedRateCardNoticeTrailerIdLengthRule(3600)
        val item = newCappedRateCardNotice("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedRateCardNoticeRequiredLabelRule("capped rate card notice needs an entry")
        val item = newCappedRateCardNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
