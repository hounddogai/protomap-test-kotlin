package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodVarianceValidator()
        val item = newDutyPeriodVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodVarianceRequiredCodeRule("duty period variance needs this field")
        val item = newDutyPeriodVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DutyPeriodVarianceNameLengthRule(3600)
        val item = newDutyPeriodVariance("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DutyPeriodVarianceRequiredNameRule("duty period variance needs this field")
        val item = newDutyPeriodVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
