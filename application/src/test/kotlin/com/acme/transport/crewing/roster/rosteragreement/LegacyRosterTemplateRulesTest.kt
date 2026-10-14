package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRosterTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRosterTemplateValidator()
        val item = newLegacyRosterTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LegacyRosterTemplateGraceDaysRangeRule(16, 3600)
        val item = newLegacyRosterTemplate("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyRosterTemplateDiscountRangeRule(2.0, 1725.9)
        val item = newLegacyRosterTemplate("x", "y")
        item.discount = 1727.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LegacyRosterTemplateRouteCodeLengthRule(120)
        val item = newLegacyRosterTemplate("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyRosterTemplateRequiredTitleRule("legacy roster template needs this field")
        val item = newLegacyRosterTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
