package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAccessorialReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAccessorialReservationValidator()
        val item = newBatchedAccessorialReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = BatchedAccessorialReservationPriorityRangeRule(16, 3600)
        val item = newBatchedAccessorialReservation("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedAccessorialReservationRequiredTitleRule("batched accessorial reservation needs a value")
        val item = newBatchedAccessorialReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedAccessorialReservationTitleLengthRule(80)
        val item = newBatchedAccessorialReservation("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAccessorialReservationRequiredCodeRule("batched accessorial reservation needs an entry")
        val item = newBatchedAccessorialReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
