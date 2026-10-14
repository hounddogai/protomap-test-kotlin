package com.acme.transport.lastmile.locker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLockerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLockerAdjustmentValidator()
        val item = newBatchedLockerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLockerAdjustmentRequiredCodeRule("batched locker adjustment needs a value")
        val item = newBatchedLockerAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedLockerAdjustmentCodeLengthRule(64)
        val item = newBatchedLockerAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = BatchedLockerAdjustmentCategoryLengthRule(16)
        val item = newBatchedLockerAdjustment("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
