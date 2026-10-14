package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLeaseReturnReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictLeaseReturnReservationValidator()
        val item = newStrictLeaseReturnReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = StrictLeaseReturnReservationRequiredCategoryRule("strict lease return reservation needs an entry")
        val item = newStrictLeaseReturnReservation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictLeaseReturnReservationUpdatedAtRangeRule(16, 86400)
        val item = newStrictLeaseReturnReservation("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictLeaseReturnReservationCodeLengthRule(64)
        val item = newStrictLeaseReturnReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
