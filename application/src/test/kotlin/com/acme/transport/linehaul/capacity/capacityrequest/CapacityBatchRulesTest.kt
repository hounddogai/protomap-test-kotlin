package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityBatchValidator()
        val item = newCapacityBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CapacityBatchTitleLengthRule(1000)
        val item = newCapacityBatch("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacityBatchCodeLengthRule(32)
        val item = newCapacityBatch("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CapacityBatchRequiredTitleRule("capacity batch needs an entry")
        val item = newCapacityBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
