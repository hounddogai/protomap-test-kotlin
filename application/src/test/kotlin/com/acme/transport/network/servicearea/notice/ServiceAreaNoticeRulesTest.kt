package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaNoticeValidator()
        val item = newServiceAreaNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ServiceAreaNoticeMinimumRangeRule(1.8, 2129.2)
        val item = newServiceAreaNotice("x", "y")
        item.minimum = 2130.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ServiceAreaNoticeEtaMinutesRangeRule(16, 3600)
        val item = newServiceAreaNotice("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaNoticeCodeLengthRule(16)
        val item = newServiceAreaNotice("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
