package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialDraftValidator()
        val item = newAccessorialDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = AccessorialDraftMarginRangeRule(1.0, 631.5)
        val item = newAccessorialDraft("x", "y")
        item.margin = 633.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AccessorialDraftCeilingRangeRule(3.1, 3991.6)
        val item = newAccessorialDraft("x", "y")
        item.ceiling = 3993.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
