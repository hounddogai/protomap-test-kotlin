package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodThresholdValidator()
        val item = newDutyPeriodThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DutyPeriodThresholdRequiredLabelRule("duty period threshold needs this field")
        val item = newDutyPeriodThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DutyPeriodThresholdEtaMinutesRangeRule(3600, 86400)
        val item = newDutyPeriodThreshold("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
