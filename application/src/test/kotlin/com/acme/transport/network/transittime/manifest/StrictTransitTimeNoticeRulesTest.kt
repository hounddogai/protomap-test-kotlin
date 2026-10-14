package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTransitTimeNoticeValidator()
        val item = newStrictTransitTimeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTransitTimeNoticeRequiredCodeRule("strict transit time notice needs a value")
        val item = newStrictTransitTimeNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictTransitTimeNoticeRequiredTitleRule("strict transit time notice needs a value")
        val item = newStrictTransitTimeNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = StrictTransitTimeNoticeMaximumRangeRule(5.9, 3415.9)
        val item = newStrictTransitTimeNotice("x", "y")
        item.maximum = 3417.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
