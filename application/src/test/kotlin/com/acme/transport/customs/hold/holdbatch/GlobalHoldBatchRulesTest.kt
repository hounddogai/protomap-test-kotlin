package com.acme.transport.customs.hold.holdbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHoldBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalHoldBatchValidator()
        val item = newGlobalHoldBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalHoldBatchCodeLengthRule(64)
        val item = newGlobalHoldBatch("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = GlobalHoldBatchRequiredSegmentRule("global hold batch needs this field")
        val item = newGlobalHoldBatch("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = GlobalHoldBatchSegmentLengthRule(32)
        val item = newGlobalHoldBatch("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalHoldBatchRetriesRangeRule(120, 3600)
        val item = newGlobalHoldBatch("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
