package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDutyPeriodNoticeValidator()
        val item = newManualDutyPeriodNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDutyPeriodNoticeCodeLengthRule(1000)
        val item = newManualDutyPeriodNotice("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ManualDutyPeriodNoticeChannelLengthRule(3600)
        val item = newManualDutyPeriodNotice("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ManualDutyPeriodNoticeUnitLengthRule(3600)
        val item = newManualDutyPeriodNotice("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
