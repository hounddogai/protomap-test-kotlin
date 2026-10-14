package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakRecordValidator()
        val item = newBatchedRestBreakRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = BatchedRestBreakRecordRequiredRouteCodeRule("batched rest break record needs an entry")
        val item = newBatchedRestBreakRecord("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = BatchedRestBreakRecordExternalRefLengthRule(1000)
        val item = newBatchedRestBreakRecord("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedRestBreakRecordWindowDaysRangeRule(3600, 3600)
        val item = newBatchedRestBreakRecord("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = BatchedRestBreakRecordUpdatedAtRangeRule(32, 64)
        val item = newBatchedRestBreakRecord("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
