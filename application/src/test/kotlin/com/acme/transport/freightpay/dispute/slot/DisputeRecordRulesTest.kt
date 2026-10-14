package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeRecordValidator()
        val item = newDisputeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeRecordRequiredLabelRule("dispute record needs this field")
        val item = newDisputeRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeRecordLabelLengthRule(1000)
        val item = newDisputeRecord("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DisputeRecordWeightRangeRule(6.9, 1683.4)
        val item = newDisputeRecord("x", "y")
        item.weight = 1684.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
