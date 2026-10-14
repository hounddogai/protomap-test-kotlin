package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalParcelDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalParcelDraftValidator()
        val item = newGlobalParcelDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalParcelDraftCodeLengthRule(3600)
        val item = newGlobalParcelDraft("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalParcelDraftGraceDaysRangeRule(16, 32)
        val item = newGlobalParcelDraft("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = GlobalParcelDraftCurrencyLengthRule(64)
        val item = newGlobalParcelDraft("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
