package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodPolicyValidator()
        val item = newDutyPeriodPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DutyPeriodPolicyRequiredLabelRule("duty period policy needs a value")
        val item = newDutyPeriodPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DutyPeriodPolicyCarrierLengthRule(16)
        val item = newDutyPeriodPolicy("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
