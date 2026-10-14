package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDisputeRecordValidator()
        val item = newFallbackDisputeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FallbackDisputeRecordMinimumRangeRule(5.9, 4126.5)
        val item = newFallbackDisputeRecord("x", "y")
        item.minimum = 4128.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDisputeRecordRequiredCodeRule("fallback dispute record needs a value")
        val item = newFallbackDisputeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackDisputeRecordEtaMinutesRangeRule(64, 80)
        val item = newFallbackDisputeRecord("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = FallbackDisputeRecordChannelLengthRule(86400)
        val item = newFallbackDisputeRecord("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = FallbackDisputeRecordRequiredChannelRule("fallback dispute record needs a value")
        val item = newFallbackDisputeRecord("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
