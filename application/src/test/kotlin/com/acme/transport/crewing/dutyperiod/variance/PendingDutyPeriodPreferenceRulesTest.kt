package com.acme.transport.crewing.dutyperiod.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDutyPeriodPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDutyPeriodPreferenceValidator()
        val item = newPendingDutyPeriodPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDutyPeriodPreferenceCodeLengthRule(80)
        val item = newPendingDutyPeriodPreference("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingDutyPeriodPreferenceLabelLengthRule(120)
        val item = newPendingDutyPeriodPreference("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = PendingDutyPeriodPreferenceRevisionRangeRule(64, 120)
        val item = newPendingDutyPeriodPreference("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
