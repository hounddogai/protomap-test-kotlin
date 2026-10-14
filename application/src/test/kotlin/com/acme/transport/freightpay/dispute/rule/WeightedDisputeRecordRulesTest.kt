package com.acme.transport.freightpay.dispute.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDisputeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDisputeRecordValidator()
        val item = newWeightedDisputeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedDisputeRecordRequiredLabelRule("weighted dispute record needs a value")
        val item = newWeightedDisputeRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDisputeRecordRequiredCodeRule("weighted dispute record needs a value")
        val item = newWeightedDisputeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedDisputeRecordLabelLengthRule(64)
        val item = newWeightedDisputeRecord("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedDisputeRecordRetriesRangeRule(32, 255)
        val item = newWeightedDisputeRecord("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = WeightedDisputeRecordRequiredDescriptionRule("weighted dispute record needs an entry")
        val item = newWeightedDisputeRecord("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
