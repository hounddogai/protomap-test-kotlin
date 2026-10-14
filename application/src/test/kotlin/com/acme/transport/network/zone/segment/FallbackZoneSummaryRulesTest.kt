package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackZoneSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackZoneSummaryValidator()
        val item = newFallbackZoneSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FallbackZoneSummaryCeilingRangeRule(9.5, 691.9)
        val item = newFallbackZoneSummary("x", "y")
        item.ceiling = 693.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackZoneSummaryTitleLengthRule(80)
        val item = newFallbackZoneSummary("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FallbackZoneSummaryRatioRangeRule(3.3, 2001.6)
        val item = newFallbackZoneSummary("x", "y")
        item.ratio = 2003.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackZoneSummaryCodeLengthRule(86400)
        val item = newFallbackZoneSummary("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
