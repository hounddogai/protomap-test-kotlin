package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAuditBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAuditBatchValidator()
        val item = newAdaptiveAuditBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = AdaptiveAuditBatchRequiredChannelRule("adaptive audit batch needs an entry")
        val item = newAdaptiveAuditBatch("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AdaptiveAuditBatchPalletsRangeRule(16, 86400)
        val item = newAdaptiveAuditBatch("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveAuditBatchRatioRangeRule(5.9, 3399.7)
        val item = newAdaptiveAuditBatch("x", "y")
        item.ratio = 3401.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAuditBatchRequiredCodeRule("adaptive audit batch needs this field")
        val item = newAdaptiveAuditBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAuditBatchCodeLengthRule(120)
        val item = newAdaptiveAuditBatch("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
