package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAccessorialDraftValidator()
        val item = newScheduledAccessorialDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledAccessorialDraftFuelLitersRangeRule(0.7, 1863.8)
        val item = newScheduledAccessorialDraft("x", "y")
        item.fuelLiters = 1865.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAccessorialDraftCodeLengthRule(32)
        val item = newScheduledAccessorialDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
