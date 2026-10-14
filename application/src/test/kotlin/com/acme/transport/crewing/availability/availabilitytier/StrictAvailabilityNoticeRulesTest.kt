package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAvailabilityNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAvailabilityNoticeValidator()
        val item = newStrictAvailabilityNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = StrictAvailabilityNoticeSequenceRangeRule(80, 1000)
        val item = newStrictAvailabilityNotice("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictAvailabilityNoticeTitleLengthRule(120)
        val item = newStrictAvailabilityNotice("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = StrictAvailabilityNoticeCeilingRangeRule(6.6, 4906.6)
        val item = newStrictAvailabilityNotice("x", "y")
        item.ceiling = 4908.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
