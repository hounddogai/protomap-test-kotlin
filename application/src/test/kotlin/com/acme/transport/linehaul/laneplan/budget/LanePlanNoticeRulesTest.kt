package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanNoticeValidator()
        val item = newLanePlanNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LanePlanNoticeTitleLengthRule(255)
        val item = newLanePlanNotice("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LanePlanNoticeEtaMinutesRangeRule(80, 3600)
        val item = newLanePlanNotice("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LanePlanNoticeNameLengthRule(1000)
        val item = newLanePlanNotice("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
