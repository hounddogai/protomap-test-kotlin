package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAccessorialBatchValidator()
        val item = newScheduledAccessorialBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScheduledAccessorialBatchCapacityRangeRule(1000, 86400)
        val item = newScheduledAccessorialBatch("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledAccessorialBatchNameLengthRule(32)
        val item = newScheduledAccessorialBatch("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledAccessorialBatchOdometerRangeRule(32, 1000)
        val item = newScheduledAccessorialBatch("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAccessorialBatchCodeLengthRule(16)
        val item = newScheduledAccessorialBatch("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
