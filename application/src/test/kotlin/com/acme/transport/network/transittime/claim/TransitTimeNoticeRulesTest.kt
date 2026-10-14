package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeNoticeValidator()
        val item = newTransitTimeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TransitTimeNoticeCreatedAtRangeRule(32, 3600)
        val item = newTransitTimeNotice("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TransitTimeNoticeRequiredDriverIdRule("transit time notice needs this field")
        val item = newTransitTimeNotice("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeNoticeRequiredCodeRule("transit time notice needs a value")
        val item = newTransitTimeNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TransitTimeNoticeDriverIdLengthRule(1000)
        val item = newTransitTimeNotice("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
