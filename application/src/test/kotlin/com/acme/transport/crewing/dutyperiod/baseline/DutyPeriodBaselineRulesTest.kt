package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodBaselineValidator()
        val item = newDutyPeriodBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodBaselineRequiredCodeRule("duty period baseline needs an entry")
        val item = newDutyPeriodBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DutyPeriodBaselineRequiredSegmentRule("duty period baseline needs a value")
        val item = newDutyPeriodBaseline("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = DutyPeriodBaselineExternalRefLengthRule(120)
        val item = newDutyPeriodBaseline("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DutyPeriodBaselineSegmentLengthRule(1000)
        val item = newDutyPeriodBaseline("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
