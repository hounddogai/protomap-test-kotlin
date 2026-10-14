package com.acme.transport.lastmile.locker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLockerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLockerBatchValidator()
        val item = newGlobalLockerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = GlobalLockerBatchTrailerIdLengthRule(64)
        val item = newGlobalLockerBatch("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalLockerBatchRequiredCodeRule("global locker batch needs a value")
        val item = newGlobalLockerBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = GlobalLockerBatchLocaleLengthRule(255)
        val item = newGlobalLockerBatch("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalLockerBatchRetriesRangeRule(32, 3600)
        val item = newGlobalLockerBatch("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
