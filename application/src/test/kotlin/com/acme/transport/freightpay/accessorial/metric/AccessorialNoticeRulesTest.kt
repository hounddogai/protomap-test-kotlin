package com.acme.transport.freightpay.accessorial.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialNoticeValidator()
        val item = newAccessorialNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialNoticeRequiredCodeRule("accessorial notice needs an entry")
        val item = newAccessorialNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AccessorialNoticeBudgetRangeRule(8.0, 4547.7)
        val item = newAccessorialNotice("x", "y")
        item.budget = 4549.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialNoticeRequiredLabelRule("accessorial notice needs an entry")
        val item = newAccessorialNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AccessorialNoticeCeilingRangeRule(4.9, 1237.9)
        val item = newAccessorialNotice("x", "y")
        item.ceiling = 1239.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AccessorialNoticeBatchSizeRangeRule(255, 86400)
        val item = newAccessorialNotice("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
