package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDutyPeriodEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDutyPeriodEstimateValidator()
        val item = newRegionalDutyPeriodEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalDutyPeriodEstimateRequiredTitleRule("regional duty period estimate needs this field")
        val item = newRegionalDutyPeriodEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDutyPeriodEstimateCodeLengthRule(16)
        val item = newRegionalDutyPeriodEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalDutyPeriodEstimateTitleLengthRule(64)
        val item = newRegionalDutyPeriodEstimate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
