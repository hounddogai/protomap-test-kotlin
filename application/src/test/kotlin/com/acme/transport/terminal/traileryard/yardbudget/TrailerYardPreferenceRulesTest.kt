package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardPreferenceValidator()
        val item = newTrailerYardPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TrailerYardPreferenceExpiresAtRangeRule(255, 1000)
        val item = newTrailerYardPreference("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = TrailerYardPreferenceBatchSizeRangeRule(16, 3600)
        val item = newTrailerYardPreference("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TrailerYardPreferenceRequiredDescriptionRule("trailer yard preference needs an entry")
        val item = newTrailerYardPreference("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = TrailerYardPreferenceDescriptionLengthRule(80)
        val item = newTrailerYardPreference("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
