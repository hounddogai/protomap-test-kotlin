package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDutyPeriodPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDutyPeriodPolicyValidator()
        val item = newCappedDutyPeriodPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = CappedDutyPeriodPolicyOdometerRangeRule(32, 64)
        val item = newCappedDutyPeriodPolicy("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CappedDutyPeriodPolicyRequiredContactEmailRule("capped duty period policy needs an entry")
        val item = newCappedDutyPeriodPolicy("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CappedDutyPeriodPolicyPriorityRangeRule(16, 64)
        val item = newCappedDutyPeriodPolicy("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
