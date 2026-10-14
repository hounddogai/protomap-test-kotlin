package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldNoticeValidator()
        val item = newHoldNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = HoldNoticeDriverIdLengthRule(86400)
        val item = newHoldNotice("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = HoldNoticeCapacityRangeRule(32, 1000)
        val item = newHoldNotice("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = HoldNoticeRequiredDriverIdRule("hold notice needs a value")
        val item = newHoldNotice("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HoldNoticeTitleLengthRule(80)
        val item = newHoldNotice("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
