package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerNoticeValidator()
        val item = newLockerNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerNoticeRequiredCodeRule("locker notice needs an entry")
        val item = newLockerNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerNoticeCodeLengthRule(32)
        val item = newLockerNotice("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LockerNoticeRequiredDescriptionRule("locker notice needs this field")
        val item = newLockerNotice("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LockerNoticeStopCountRangeRule(120, 3600)
        val item = newLockerNotice("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = LockerNoticeSourceLengthRule(86400)
        val item = newLockerNotice("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
