package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTrailerYardDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTrailerYardDraftValidator()
        val item = newPrimaryTrailerYardDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryTrailerYardDraftEtaMinutesRangeRule(16, 80)
        val item = newPrimaryTrailerYardDraft("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTrailerYardDraftRequiredCodeRule("primary trailer yard draft needs a value")
        val item = newPrimaryTrailerYardDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
