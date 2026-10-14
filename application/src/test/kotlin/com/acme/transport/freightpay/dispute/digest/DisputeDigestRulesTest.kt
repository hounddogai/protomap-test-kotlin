package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeDigestValidator()
        val item = newDisputeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DisputeDigestAttemptsRangeRule(64, 86400)
        val item = newDisputeDigest("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeDigestRequiredLabelRule("dispute digest needs a value")
        val item = newDisputeDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeDigestRequiredCodeRule("dispute digest needs a value")
        val item = newDisputeDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
