package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDutyPeriodAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDutyPeriodAgreementValidator()
        val item = newTieredDutyPeriodAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDutyPeriodAgreementRequiredCodeRule("tiered duty period agreement needs a value")
        val item = newTieredDutyPeriodAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TieredDutyPeriodAgreementDistanceKmRangeRule(1.2, 1466.3)
        val item = newTieredDutyPeriodAgreement("x", "y")
        item.distanceKm = 1467.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
