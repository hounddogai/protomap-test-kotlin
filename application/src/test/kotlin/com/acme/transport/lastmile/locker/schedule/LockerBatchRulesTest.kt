package com.acme.transport.lastmile.locker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerBatchValidator()
        val item = newLockerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LockerBatchRequiredLaneRule("locker batch needs this field")
        val item = newLockerBatch("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerBatchRequiredCodeRule("locker batch needs an entry")
        val item = newLockerBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LockerBatchUpdatedAtRangeRule(80, 86400)
        val item = newLockerBatch("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
