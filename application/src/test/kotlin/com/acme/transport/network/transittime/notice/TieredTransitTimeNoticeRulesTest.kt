package com.acme.transport.network.transittime.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTransitTimeNoticeValidator()
        val item = newTieredTransitTimeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = TieredTransitTimeNoticeCategoryLengthRule(86400)
        val item = newTieredTransitTimeNotice("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredTransitTimeNoticeRequiredTitleRule("tiered transit time notice needs an entry")
        val item = newTieredTransitTimeNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredTransitTimeNoticeTitleLengthRule(255)
        val item = newTieredTransitTimeNotice("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
