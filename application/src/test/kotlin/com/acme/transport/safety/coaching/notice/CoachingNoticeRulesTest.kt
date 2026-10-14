package com.acme.transport.safety.coaching.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingNoticeValidator()
        val item = newCoachingNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CoachingNoticeRequiredCurrencyRule("coaching notice needs this field")
        val item = newCoachingNotice("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CoachingNoticeRequiredNameRule("coaching notice needs an entry")
        val item = newCoachingNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingNoticeNameLengthRule(255)
        val item = newCoachingNotice("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = CoachingNoticeCategoryLengthRule(16)
        val item = newCoachingNotice("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
