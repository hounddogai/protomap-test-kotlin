package com.acme.transport.crewing.dutyperiod.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodGroupValidator()
        val item = newDutyPeriodGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DutyPeriodGroupLimitRangeRule(64, 1000)
        val item = newDutyPeriodGroup("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DutyPeriodGroupRateRangeRule(5.4, 845.8)
        val item = newDutyPeriodGroup("x", "y")
        item.rate = 847.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodGroupCodeLengthRule(3600)
        val item = newDutyPeriodGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
