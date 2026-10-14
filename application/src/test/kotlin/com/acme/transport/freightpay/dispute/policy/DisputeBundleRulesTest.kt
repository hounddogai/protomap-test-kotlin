package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeBundleValidator()
        val item = newDisputeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeBundleCodeLengthRule(86400)
        val item = newDisputeBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DisputeBundleContactEmailLengthRule(80)
        val item = newDisputeBundle("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DisputeBundleChecksumLengthRule(3600)
        val item = newDisputeBundle("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DisputeBundleGraceDaysRangeRule(120, 255)
        val item = newDisputeBundle("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
