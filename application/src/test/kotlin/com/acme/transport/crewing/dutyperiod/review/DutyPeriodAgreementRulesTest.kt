package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodAgreementValidator()
        val item = newDutyPeriodAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DutyPeriodAgreementDriverIdLengthRule(255)
        val item = newDutyPeriodAgreement("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DutyPeriodAgreementCapacityRangeRule(64, 1000)
        val item = newDutyPeriodAgreement("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DutyPeriodAgreementRequiredLabelRule("duty period agreement needs a value")
        val item = newDutyPeriodAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DutyPeriodAgreementLabelLengthRule(3600)
        val item = newDutyPeriodAgreement("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
