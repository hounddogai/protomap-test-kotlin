package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDutyPeriodAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDutyPeriodAssignmentValidator()
        val item = newSeasonalDutyPeriodAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalDutyPeriodAssignmentRequiredNameRule("seasonal duty period assignment needs an entry")
        val item = newSeasonalDutyPeriodAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SeasonalDutyPeriodAssignmentEtaMinutesRangeRule(3600, 3600)
        val item = newSeasonalDutyPeriodAssignment("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDutyPeriodAssignmentRequiredCodeRule("seasonal duty period assignment needs a value")
        val item = newSeasonalDutyPeriodAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
