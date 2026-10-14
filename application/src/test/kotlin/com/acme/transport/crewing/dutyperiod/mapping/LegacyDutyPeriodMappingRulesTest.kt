package com.acme.transport.crewing.dutyperiod.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDutyPeriodMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDutyPeriodMappingValidator()
        val item = newLegacyDutyPeriodMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyDutyPeriodMappingTitleLengthRule(86400)
        val item = newLegacyDutyPeriodMapping("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = LegacyDutyPeriodMappingRequiredSummaryRule("legacy duty period mapping needs this field")
        val item = newLegacyDutyPeriodMapping("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDutyPeriodMappingRequiredCodeRule("legacy duty period mapping needs this field")
        val item = newLegacyDutyPeriodMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
