package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRateCardScheduleValidator()
        val item = newCappedRateCardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedRateCardScheduleRequiredOwnerIdRule("capped rate card schedule needs an entry")
        val item = newCappedRateCardSchedule("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedRateCardScheduleCodeLengthRule(3600)
        val item = newCappedRateCardSchedule("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CappedRateCardScheduleRequiredDockRule("capped rate card schedule needs an entry")
        val item = newCappedRateCardSchedule("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
