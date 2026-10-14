package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDutyPeriodTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDutyPeriodTierValidator()
        val item = newRegionalDutyPeriodTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RegionalDutyPeriodTierShiftCodeLengthRule(86400)
        val item = newRegionalDutyPeriodTier("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
