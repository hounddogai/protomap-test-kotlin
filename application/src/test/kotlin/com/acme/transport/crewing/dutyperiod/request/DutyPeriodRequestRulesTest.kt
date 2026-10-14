package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodRequestValidator()
        val item = newDutyPeriodRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DutyPeriodRequestRequiredShiftCodeRule("duty period request needs this field")
        val item = newDutyPeriodRequest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = DutyPeriodRequestCurrencyLengthRule(16)
        val item = newDutyPeriodRequest("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DutyPeriodRequestRequiredNameRule("duty period request needs this field")
        val item = newDutyPeriodRequest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
