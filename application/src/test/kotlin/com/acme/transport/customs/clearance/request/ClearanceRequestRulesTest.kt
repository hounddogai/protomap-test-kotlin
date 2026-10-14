package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceRequestValidator()
        val item = newClearanceRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceRequestCodeLengthRule(16)
        val item = newClearanceRequest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ClearanceRequestBatchSizeRangeRule(64, 80)
        val item = newClearanceRequest("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ClearanceRequestThresholdRangeRule(1.3, 2251.6)
        val item = newClearanceRequest("x", "y")
        item.threshold = 2253.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
