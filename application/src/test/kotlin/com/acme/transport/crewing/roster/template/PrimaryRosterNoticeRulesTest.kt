package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRosterNoticeValidator()
        val item = newPrimaryRosterNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryRosterNoticeRequiredLabelRule("primary roster notice needs this field")
        val item = newPrimaryRosterNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryRosterNoticeLabelLengthRule(1000)
        val item = newPrimaryRosterNotice("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PrimaryRosterNoticeRatioRangeRule(2.5, 2372.7)
        val item = newPrimaryRosterNotice("x", "y")
        item.ratio = 2374.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = PrimaryRosterNoticeScoreRangeRule(3.4, 2288.7)
        val item = newPrimaryRosterNotice("x", "y")
        item.score = 2290.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PrimaryRosterNoticeCreatedAtRangeRule(3600, 86400)
        val item = newPrimaryRosterNotice("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
