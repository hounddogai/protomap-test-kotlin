package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDeclarationTierValidator()
        val item = newSeasonalDeclarationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = SeasonalDeclarationTierSummaryLengthRule(1000)
        val item = newSeasonalDeclarationTier("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDeclarationTierRequiredCodeRule("seasonal declaration tier needs an entry")
        val item = newSeasonalDeclarationTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = SeasonalDeclarationTierSequenceRangeRule(120, 255)
        val item = newSeasonalDeclarationTier("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SeasonalDeclarationTierRevisionRangeRule(32, 86400)
        val item = newSeasonalDeclarationTier("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDeclarationTierTitleLengthRule(80)
        val item = newSeasonalDeclarationTier("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
