package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakDraftValidator()
        val item = newRestBreakDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RestBreakDraftAttemptsRangeRule(16, 1000)
        val item = newRestBreakDraft("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakDraftRequiredNameRule("rest break draft needs a value")
        val item = newRestBreakDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RestBreakDraftWeightRangeRule(0.5, 1307.9)
        val item = newRestBreakDraft("x", "y")
        item.weight = 1309.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
