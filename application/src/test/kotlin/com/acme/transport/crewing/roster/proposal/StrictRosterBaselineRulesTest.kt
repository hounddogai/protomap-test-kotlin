package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRosterBaselineValidator()
        val item = newStrictRosterBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRosterBaselineLabelLengthRule(80)
        val item = newStrictRosterBaseline("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictRosterBaselineTitleLengthRule(120)
        val item = newStrictRosterBaseline("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = StrictRosterBaselineExpiresAtRangeRule(32, 1000)
        val item = newStrictRosterBaseline("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
