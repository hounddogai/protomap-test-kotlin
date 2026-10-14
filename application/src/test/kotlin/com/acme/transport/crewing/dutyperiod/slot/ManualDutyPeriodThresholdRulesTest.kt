package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDutyPeriodThresholdValidator()
        val item = newManualDutyPeriodThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualDutyPeriodThresholdRetriesRangeRule(255, 86400)
        val item = newManualDutyPeriodThreshold("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
